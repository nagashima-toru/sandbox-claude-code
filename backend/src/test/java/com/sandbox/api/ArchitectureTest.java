package com.sandbox.api;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
@AnalyzeClasses(packages = "com.sandbox.api")
class ArchitectureTest {
  @ArchTest
  static final ArchRule domainLayerShouldNotDependOnOtherLayers =
      noClasses()
          .that()
          .resideInAnyPackage("..domain..")
          .should()
          .dependOnClassesThat()
          .resideInAnyPackage("..application..", "..infrastructure..", "..presentation..")
          .allowEmptyShould(true)
          .because("Domain layer must not depend on outer layers");
  static final ArchRule applicationLayerShouldNotDependOnInfrastructure =
          .resideInAnyPackage("..application..")
          .resideInAnyPackage("..infrastructure..", "..presentation..")
          .because("Application layer must not depend on infrastructure or presentation layers");
  static final ArchRule repositoriesShouldBeInterfaces =
      classes()
          .haveNameMatching(".*Repository")
          .and()
          .resideInAnyPackage("..domain.repository..")
          .beInterfaces()
          .because("Repositories in domain layer should be interfaces");
}
