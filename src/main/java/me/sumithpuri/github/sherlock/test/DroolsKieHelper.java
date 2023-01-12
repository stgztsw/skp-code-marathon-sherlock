package me.sumithpuri.github.sherlock.test;

import org.drools.core.impl.KnowledgeBaseImpl;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.conf.EventProcessingOption;
import org.kie.api.definition.type.FactType;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.conf.ClockTypeOption;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.utils.KieHelper;

public class DroolsKieHelper {

  private final KieBase kieBase;

  private KieSession kieSession;

  private KieSessionConfiguration config;

  public DroolsKieHelper() {
    KieHelper kieHelper = new KieHelper();
    kieBase = kieHelper.build(EventProcessingOption.STREAM);
    config = KieServices.Factory.get().newKieSessionConfiguration();
  }

  public void addPseudoClockOption() {
    config.setOption( ClockTypeOption.get( "pseudo" ) );
  }

  public KieSession buildSession() {
    this.kieSession = kieBase.newKieSession(config, null);
    return this.kieSession;
  }

  public DroolsKieHelper addRule(String packageName, String ruleName) {
    KnowledgeBuilder kb = KnowledgeBuilderFactory.newKnowledgeBuilder();
    kb.add(ResourceFactory.newClassPathResource(packageName + "/" + ruleName + ".drl"), ResourceType.DRL);
    KnowledgeBaseImpl kieBaseImpl = (KnowledgeBaseImpl) kieBase;
    kieBaseImpl.addKnowledgePackages(kb.getKnowledgePackages());
    return this;
  }

  public KieSession getKieSession() {
    return kieSession;
  }

  public FactType getFactType(String packageName, String factName) {
    return kieBase.getFactType(packageName, factName);
  }
}
