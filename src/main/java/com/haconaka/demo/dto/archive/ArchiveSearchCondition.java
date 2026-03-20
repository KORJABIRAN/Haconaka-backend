package com.haconaka.demo.dto.archive;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ArchiveSearchCondition {
    private List<String> member = new ArrayList<>();
    private List<String> category = new ArrayList<>();
    private String search = "";
    private String filter = "title";
}