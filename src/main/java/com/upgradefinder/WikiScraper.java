package com.upgradefinder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class WikiScraper {

    private List<Weapon> allWeapons;

    public void loadWeaponsFromClasspath() throws IOException {
        this.allWeapons = new ArrayList<>();
        InputStream is = WikiScraper.class.getResourceAsStream("/Weapon_slot_table_-_OSRS_Wiki.htm");
        if (is == null) {
            System.err.println("Weapon table HTML not found in resources!");
            return;
        }

        Document doc = Jsoup.parse(is, StandardCharsets.UTF_8.name(), "");

        Elements tableRows = doc.select(".wikitable tbody tr");

        for (int i = 1; i < tableRows.size(); i++) {
            Element row = tableRows.get(i);
            Elements cells = row.select("td");

            if (cells.size() >= 19) { // Ensure row has enough columns
                Weapon weapon = new Weapon();
                weapon.setName(cells.get(1).text());
                weapon.setStabAttack(parseStat(cells.get(3).text()));
                weapon.setSlashAttack(parseStat(cells.get(4).text()));
                weapon.setCrushAttack(parseStat(cells.get(5).text()));
                weapon.setMagicAttack(parseStat(cells.get(6).text()));
                weapon.setRangedAttack(parseStat(cells.get(7).text()));
                weapon.setStrengthBonus(parseStat(cells.get(13).text()));
                weapon.setAttackSpeed(parseStat(cells.get(18).text()));
                allWeapons.add(weapon);
            }
        }
    }

    public List<Weapon> findUpgrades(String weaponName, String style) {
        Weapon currentWeapon = allWeapons.stream()
                .filter(w -> w.getName().equalsIgnoreCase(weaponName))
                .findFirst()
                .orElse(null);

        if (currentWeapon == null) {
            return new ArrayList<>(); // Return empty list if weapon not found
        }

        switch (style.toLowerCase()) {
            case "slash":
                return allWeapons.stream()
                        .filter(w -> w.getSlashAttack() > currentWeapon.getSlashAttack() && w.getStrengthBonus() >= currentWeapon.getStrengthBonus())
                        .sorted(Comparator.comparing(Weapon::getSlashAttack).thenComparing(Weapon::getStrengthBonus).reversed())
                        .collect(Collectors.toList());
            case "stab":
                return allWeapons.stream()
                        .filter(w -> w.getStabAttack() > currentWeapon.getStabAttack() && w.getStrengthBonus() >= currentWeapon.getStrengthBonus())
                        .sorted(Comparator.comparing(Weapon::getStabAttack).thenComparing(Weapon::getStrengthBonus).reversed())
                        .collect(Collectors.toList());
            case "crush":
                return allWeapons.stream()
                        .filter(w -> w.getCrushAttack() > currentWeapon.getCrushAttack() && w.getStrengthBonus() >= currentWeapon.getStrengthBonus())
                        .sorted(Comparator.comparing(Weapon::getCrushAttack).thenComparing(Weapon::getStrengthBonus).reversed())
                        .collect(Collectors.toList());
            default:
                return new ArrayList<>();
        }
    }




    private int parseStat(String statString) {
        try {
            return Integer.parseInt(statString.replace("+", "").trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}