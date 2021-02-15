package fr.redfroggy.bdd.glue;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.Assert;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.cloud.stream.test.binder.MessageCollector;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Spring cloud cucumber steps
 * Will push or poll data for a given queue
 * Please see {@link MessageCollector} and {@link MessageChannel}
 */
public class MessagingBddStepDefinition extends AbstractBddStepDefinition{

    private final MessageCollector collector;
    private final List<MessageChannel> channels;

    private BlockingQueue<Message<?>> messages;

    public MessagingBddStepDefinition(TestRestTemplate testRestTemplate, MessageCollector collector,
                                      List<MessageChannel> channels) {
        super(testRestTemplate);
        this.collector = collector;
        this.channels = channels;
    }

    @Given("^I PUSH to queue (.*) with message (.*)$")
    public void pushToQueue(String channelName, String body) {
        MessageChannel channel = getChannelByName(channelName);
        Assert.assertNotNull(channel);

        channel.send(new GenericMessage<>(body));
    }

    @When("^I POLL first message from queue (.*)$")
    public void pollFromQueue(String channelName) {
        MessageChannel channel = getChannelByName(channelName);
        Assert.assertNotNull(channel);

        messages = collector.forChannel(channel);
        Assert.assertNotNull(messages);
        Assert.assertFalse(messages.isEmpty());
    }

    @Then("^queue message body path (.*) should be (.*)$")
    public void messageBodyShouldHaveValue(String jsonPath, String value) {
        this.checkJsonPath(jsonPath, value, false);
    }

    @Override
    protected Object getPayload() {
        try {
            return objectMapper.readValue(String.valueOf(messages.take().getPayload()), LinkedHashMap.class);
        } catch(Exception ex) {
            return null;
        }
    }

    private MessageChannel getChannelByName(String channelName) {
        return this.channels.stream()
                .filter(directChannel -> channelName.equals(directChannel.toString()))
                .findFirst().orElse(null);
    }
}
