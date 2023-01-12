package me.sumithpuri.github.sherlock.test;

import org.drools.core.time.SessionPseudoClock;
import org.junit.Test;
import org.kie.api.definition.type.FactField;
import org.kie.api.definition.type.FactType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.EntryPoint;
import org.kie.api.time.SessionClock;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MainTest {

  private static DroolsKieHelper helper;

  private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  static {
    helper = new DroolsKieHelper();
  }

  @Test
  public void test01() throws IllegalAccessException, InstantiationException, ParseException {
    KieSession kieSession = helper.addRule("event", "test01").buildSession();
    List<Object> objects = new ArrayList<>();
    objects.add(createTestEvent("10001", "gs_001", "2022-11-21 15:00:00", "M1"));
    objects.add(createTestEvent("10002", "gs_002", "2022-11-21 15:00:00", "M2"));
    for (Object o:objects) {
      kieSession.insert(o);
      kieSession.fireAllRules();
    }
  }

  @Test
  public void test02() throws IllegalAccessException, InstantiationException, ParseException {
    KieSession kieSession = helper.addRule("event", "test02").buildSession();
    List<Object> objects = new ArrayList<>();
    objects.add(createTestEvent("10001", "gs_001", "2022-11-21 15:00:00", "M1"));
    objects.add(createTestEvent("10002", "gs_002", "2022-11-21 15:00:00", "M1"));
    objects.add(createTestEvent("10003", "gs_003", "2022-11-21 15:00:00", "M1"));
    for (Object o:objects) {
      kieSession.insert(o);
      kieSession.fireAllRules();
    }
  }

  @Test
  public void test03() throws IllegalAccessException, InstantiationException, ParseException {
    KieSession kieSession = helper.addRule("event", "test03").buildSession();
    List<Object> objects = new ArrayList<>();
    objects.add(createTestEvent("10001", "gs_001", "2022-11-21 15:00:00", "M1"));
    objects.add(createTestEvent("10002", "gs_002", "2022-11-21 15:00:01", "M1"));
    objects.add(createTestEvent("10003", "gs_003", "2022-11-21 15:00:02", "M1"));
    for (Object o:objects) {
      kieSession.insert(o);
      kieSession.fireAllRules();
    }
  }

  @Test
  public void test04() throws IllegalAccessException, InstantiationException, ParseException {
    helper.addPseudoClockOption();
    KieSession kieSession = helper.addRule("event", "test04").buildSession();
    EntryPoint ep = kieSession.getEntryPoint("IncidentStream");
    try {
      List<Object> objects = new ArrayList<>();
      objects.add(createTestEvent("10001", "gs_001", "2022-11-21 15:00:00", "M1", "1"));
      objects.add(createTestEvent("10001", "gs_002", "2022-11-21 15:00:10", "M1", "2"));
      for (Object o:objects) {
        insert(helper, kieSession, o);
        kieSession.fireAllRules();
      }
    } finally {
      kieSession.dispose();
    }

  }

  private void insert(DroolsKieHelper helper, KieSession kSession, Object o) {
    EntryPoint ep = kSession.getEntryPoint("IncidentStream");
    SessionClock clock = kSession.getSessionClock();
    SessionPseudoClock pseudoClock = (SessionPseudoClock) clock;
    FactType factType = helper.getFactType("event", "TestEvent");
    ep.insert(o);
    Date date = (Date) factType.getField("event_time").get(o);
    long advanceTime = date.getTime() - pseudoClock.getCurrentTime();
    if (advanceTime > 0) {
      pseudoClock.advanceTime(advanceTime, TimeUnit.MILLISECONDS);
    }
  }

  private Object createTestEvent(String ...params) throws IllegalAccessException, InstantiationException, ParseException {
    FactType factType = helper.getFactType("event", "TestEvent");
    Object o = factType.newInstance();
    if (factType.getFields().size() != params.length) {
      throw new RuntimeException("the size of params is difference");
    }
    int i=0;
    for (FactField f : factType.getFields()) {
      factType.set(o, f.getName(),convertType(f.getType(), params[i]));
      i++;
    }
    return o;
  }

  private Object convertType(Class clazz, String param) throws ParseException {
    if (clazz.getName().equals("java.lang.Integer")) {
      return Integer.parseInt(param);
    } else if (clazz.getName().equals("java.util.Date")) {
      return df.parse(param);
    }
    return param;
  }









}
