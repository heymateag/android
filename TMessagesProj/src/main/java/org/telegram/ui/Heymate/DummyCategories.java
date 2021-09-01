package org.telegram.ui.Heymate;

import java.util.ArrayList;
import java.util.HashMap;

public class DummyCategories {

    private static DummyCategories instance = new DummyCategories();
    static public HashMap<String, ArrayList<String>> categories;

    private DummyCategories() {
        categories = new HashMap<>();
        categories.put("Beauty & wellness", new ArrayList<String>() {{
            add("Beard trimming");
            add("Blowouts");
            add("Eyebrow service");
            add("Eyelash service");
            add("Facial treatments");
            add("Gel nail");
            add("Hair dressing");
            add("Hair dying");
            add("Makeup");
            add("Manicure");
            add("Massage");
            add("Nail art");
            add("Waxing");
        }});
        categories.put("Entertainment", new ArrayList<String>() {{
            add("Theatrical");
            add("Storytelling");
            add("Online tour");
            add("Music performance");
            add("Pawel's magic show ✨");
            add("Normal magic show");
            add("Dance performance");
            add("Light show");
            add("Charades");
        }});
        categories.put("Handyman & Repair", new ArrayList<String>() {{
            add("Painting");
            add("Lawn mowing");
            add("Carpenting");
            add("Moving");
            add("Kitchen fix");
            add("Tiling");
            add("Electrical work");
            add("Plumbing");
        }});
        categories.put("Domestic services", new ArrayList<String>() {{
            add("Cleaning");
            add("Cooking");
            add("Security");
            add("Driving");
            add("Dog walking");
            add("Laundry");
            add("Elder care");
            add("Babysit");
        }});
        categories.put("Education (upto highschool)", new ArrayList<String>() {{
            add("Elementary Math");
            add("High school math");
            add("SAT Prep Math");
            add("SAT Prep English");
            add("ACT Prep Math");
            add("ACT Prep English");
        }});
        categories.put("Language Learning", new ArrayList<String>() {{
            add("Learning English");
            add("Learning German");
            add("Learning French");
            add("Learning Spanish");
            add("Learning Chinese");
        }});
        categories.put("Professional services", new ArrayList<String>() {{
            add("Java");
            add("Web development");
            add("Python");
            add("Adobe XD");
            add("Adobe Photoshop");
        }});
    }

    public static DummyCategories getInstance() {
        return instance;
    }
}
