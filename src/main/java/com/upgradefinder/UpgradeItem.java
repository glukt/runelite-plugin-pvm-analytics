package com.upgradefinder;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpgradeItem
{
    private final String itemName;
    private final String upgradeName;
    private final String requirements;
}