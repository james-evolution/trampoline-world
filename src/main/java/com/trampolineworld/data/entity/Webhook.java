package com.trampolineworld.data.entity;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "webhooks")
public class Webhook extends AbstractEntityUUID {

  private String webhookName;
  private String webhookUrl;

  public String getWebhookName() {
    return webhookName;
  }
  public void setWebhookName(String webhookName) {
    this.webhookName = webhookName;
  }
  public String getWebhookUrl() {
    return webhookUrl;
  }
  public void setWebhookUrl(String webhookUrl) {
    this.webhookUrl = webhookUrl;
  }
}
