package com.upgradefinder;

import lombok.Data;

@Data
public class Weapon {
    private String name;
    private int stabAttack;
    private int slashAttack;
    private int crushAttack;
    private int magicAttack;
    private int rangedAttack;
    private int strengthBonus;
    private int attackSpeed;
}