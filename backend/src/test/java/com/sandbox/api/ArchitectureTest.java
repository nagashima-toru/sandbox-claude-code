package com.sandbox.api;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.sandbox.api")
class ArchitectureTest {

    @ArchTest
    static final ArchRule domainLayerShouldNotDependOnOtherLayers =
        noClasses()
            .that().resideInAnyPackage("..domain..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..application..", "..infrastructure..", "..presentation..")
            .allowEmptyShould(true)
            .because("Domain layer must not depend on outer layers");

    @ArchTest
    static final ArchRule applicationLayerShouldNotDependOnInfrastructure =
        noClasses()
            .that().resideInAnyPackage("..application..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..infrastructure..", "..presentation..")
            .allowEmptyShould(true)
            .because("Application layer must not depend on infrastructure or presentation layers");

    @ArchTest
    static final ArchRule repositoriesShouldBeInterfaces =
        classes()
            .that().haveNameMatching(".*Repository")
            .and().resideInAnyPackage("..domain.repository..")
            .should().beInterfaces()
            .allowEmptyShould(true)
            .because("Repositories in domain layer should be interfaces");
}