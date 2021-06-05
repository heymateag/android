package works.heymate.util;

import java.util.ArrayList;
import java.util.List;

public class Template {

    public interface ObjectProvider {

        Object get(Object from, String name);

        List<Object> getList(Object from, String name);

    }

    public interface ObjectBuilder {

        Object ensure(Object from, String name);

        void set(Object from, String name, String value);

    }

    public static Template parse(String pattern) {
        List<Object> sequence = new ArrayList<>();

        return parse(pattern, 0, sequence);
    }

    private static Template parse(String pattern, int position, List<Object> sequence) {
        char c = pattern.charAt(position);

        if (c == '{') {
            int end = getDynamicEnd(pattern, position + 1);

            sequence.add(new Dynamic(pattern.substring(position, end)));

            position = end;
        }
        else {
            int end = position;

            while (end < pattern.length() && pattern.charAt(end) != '{') {
                if (pattern.charAt(end) == '\\') {
                    end++;
                }

                end++;
            }

            sequence.add(pattern.substring(position, end));

            position = end;
        }

        if (position == pattern.length()) {
            return new Template(sequence);
        }
        else {
            return parse(pattern, position, sequence);
        }
    }

    private static int getDynamicEnd(String pattern, int index) {
        if (index == pattern.length()) {
            return index;
        }

        char c = pattern.charAt(index);

        while (c != '}') {
            if (c == '\\') {
                index += 2;
                continue;
            }

            if (c == '{') {
                index = getDynamicEnd(pattern, index + 1);
            }
            else {
                index++;
            }

            if (index == pattern.length()) {
                return index;
            }

            c = pattern.charAt(index);
        }

        return index + 1;
    }

    private List<Object> mSequence;

    private Template(List<Object> sequence) {
        mSequence = sequence;
    }

    public String apply(ObjectProvider provider) {
        StringBuilder sb = new StringBuilder();

        for (Object object: mSequence) {
            if (object instanceof CharSequence) {
                sb.append((CharSequence) object);
            }
            else {
                sb.append(((Dynamic) object).apply(provider));
            }
        }

        return sb.toString();
    }

    public void build(String text, ObjectBuilder builder) {
        int position = 0;

        for (int index = 0; index < mSequence.size(); index++) {
            Object part = mSequence.get(index);

            if (part instanceof CharSequence) {
                position += ((CharSequence) part).length();
            }
            else {
                Object nextPart = index == mSequence.size() - 1 ? null : mSequence.get(index + 1);

                int partEnd;

                if (nextPart instanceof CharSequence) {
                    partEnd = text.indexOf(nextPart.toString(), position);

                    if (partEnd == -1) {
                        return;
                    }
                }
                else if (nextPart == null) {
                    partEnd = text.length();
                }
                else {
                    return;
                }

                ((Dynamic) part).build(text.substring(position, partEnd), builder);

                position = partEnd;
            }

            if (position >= text.length()) {
                return;
            }
        }
    }

    private static class Dynamic {

        private final Key[] mKeys;

        private Dynamic(String s) {
            String key = s.substring(1, s.length() - 1);

            String[] keys = key.split("\\.");

            mKeys = new Key[keys.length];

            for (int i = 0; i < keys.length; i++) {
                mKeys[i] = new Key(keys[i]);
            }
        }

        public String apply(ObjectProvider provider) {
            Object object = null;

            for (int i = 0; i < mKeys.length; i++) {
                object = mKeys[i].get(provider, object);

                if (object == null) {
                    return "";
                }
            }

            return object.toString();
        }

        public void build(String text, ObjectBuilder builder) {
            Object object = null;

            for (int i = 0; i < mKeys.length - 1; i++) {
                object = mKeys[i].ensure(object, builder);
            }

            mKeys[mKeys.length - 1].build(text, object, builder);
        }

    }

    private static class Key {

        private final String mKey;
        private final boolean mHasIndex;
        private final int mIndex;

        private Key(String s) {
            if (s.endsWith("]")) {
                int start = s.lastIndexOf("[");

                mKey = s.substring(0, start);
                mHasIndex = true;
                mIndex = Integer.parseInt(s.substring(start + 1, s.length() - 1));
            }
            else {
                mKey = s;
                mHasIndex = false;
                mIndex = 0;
            }
        }

        private Object get(ObjectProvider provider, Object object) {
            if (mHasIndex) {
                List<Object> list = provider.getList(object, mKey);

                if (list == null || list.isEmpty()) {
                    return null;
                }

                int index = mIndex;

                while (index < 0) {
                    index += list.size();
                }

                if (index > list.size() - 1) {
                    return null;
                }

                return list.get(index);
            }
            else {
                return provider.get(object, mKey);
            }
        }

        private Object ensure(Object object, ObjectBuilder builder) {
            return builder.ensure(object, mKey);
        }

        private void build(String text, Object object, ObjectBuilder builder) {
            builder.set(object, mKey, text);
        }

    }

}
