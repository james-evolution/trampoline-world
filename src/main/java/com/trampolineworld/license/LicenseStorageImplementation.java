package com.trampolineworld.license;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.vaadin.collaborationengine.LicenseStorage;

public class LicenseStorageImplementation implements LicenseStorage {

  @Override
  public List<String> getUserEntries(String licenseKey, YearMonth month) {
    // TODO Auto-generated method stub
    
    return Arrays.asList("TW Admin", "TW User");
  }

  @Override
  public void addUserEntry(String licenseKey, YearMonth month, String payload) {
    // TODO Auto-generated method stub
    
  }

  @Override
  public Map<String, LocalDate> getLatestLicenseEvents(String licenseKey) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public void setLicenseEvent(String licenseKey, String eventName, LocalDate latestOccurrence) {
    // TODO Auto-generated method stub
    
  }

}
