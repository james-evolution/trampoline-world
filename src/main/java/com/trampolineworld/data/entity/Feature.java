package com.trampolineworld.data.entity;

import com.vaadin.flow.component.icon.Icon;

public class Feature {

  private String label;
  private String description;
  private Icon icon;

  public Feature() {

  }

  public Feature(String label, String description) {
    this.label = label;
    this.description = description;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public Icon getIcon() {
    return icon;
  }

  public void setIcon(Icon icon) {
    this.icon = icon;
  }
}
