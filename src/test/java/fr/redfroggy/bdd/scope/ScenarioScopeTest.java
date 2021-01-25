package fr.redfroggy.bdd.scope;

import org.junit.Assert;
import org.junit.Test;

public class ScenarioScopeTest {

    ScenarioScope scenarioScope = new ScenarioScope();

    @Test
    public void shouldBeInitialized() {
        Assert.assertNotNull(scenarioScope.getHeaders());
        Assert.assertNotNull(scenarioScope.getJsonPaths());
    }
}
