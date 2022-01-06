package works.heymate.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import works.heymate.api.APIObject;

abstract public class DefaultObjectProvider implements Template.ObjectProvider {

    @Override
    public Object get(Object from, String name) {
        if (from == null) {
            return getRootObject(name);
        }

        JSONObject json = null;

        if (from instanceof String) {
            try {
                json = new JSONObject((String) from);
            } catch (JSONException e) { }
        }

        if (from instanceof JSONObject) {
            json = (JSONObject) from;
        }

        if (from instanceof APIObject) {
            json = ((APIObject) from).asJSON();
        }

        if (json != null) {
            try {
                return json.get(name);
            } catch (JSONException e) {
                return null;
            }
        }

        if (from instanceof Map) {
            return ((Map<?, ?>) from).get(name);
        }

        String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
        Method method = findMethod(from.getClass(), methodName);

        if (method != null) {
            try {
                return method.invoke(from);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        method = findMethod(from.getClass(), name);

        if (method != null) {
            try {
                return method.invoke(from);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        Field field = findField(from.getClass(), name);

        if (field != null) {
            try {
                return field.get(from);
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }

        return null;
    }

    @Override
    public List<Object> getList(Object from, String name) {
        return null;
    }

    abstract protected Object getRootObject(String name);

    private Method findMethod(Class<?> clazz, String name) {
        try {
            return clazz.getMethod(name);
        } catch (Throwable t) { }

        if (clazz.equals(Object.class)) {
            return null;
        }

        return findMethod(clazz.getSuperclass(), name);
    }

    private Field findField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);
            return field;
        } catch (Throwable t) { }

        if (clazz.equals(Object.class)) {
            return null;
        }

        return findField(clazz.getSuperclass(), name);
    }

}
