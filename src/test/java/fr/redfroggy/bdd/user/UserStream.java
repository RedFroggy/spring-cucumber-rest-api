package fr.redfroggy.bdd.user;

import org.junit.Assert;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.annotation.Input;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.support.GenericMessage;

@EnableBinding(UserStream.Process.class)
public class UserStream {

    private final UserStream.Process process;

    public UserStream(UserStream.Process process) {
        this.process = process;
    }

    @StreamListener(Process.INPUT_VALID_USER)
    public void validPaymentIdentityFormat(UserDTO user) {
        Assert.assertNotNull(user);
        boolean valid = user.getId() != null && user.getLastName() != null
                && user.getFirstName() != null && user.getAge() > 0;
        user.setStatus("VALID");

        // send result to output queue
        this.process.validUser().send(new GenericMessage<>(user));
    }

    public interface Process {
        String INPUT_VALID_USER = "input-valid-user";
        String OUTPUT_VALID_USER = "output-valid-user";

        @Input(INPUT_VALID_USER)
        MessageChannel getUser();

        @Output(OUTPUT_VALID_USER)
        MessageChannel validUser();
    }
}
