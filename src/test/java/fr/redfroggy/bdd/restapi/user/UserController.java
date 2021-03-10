package fr.redfroggy.bdd.restapi.user;

import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.junit.Assert;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import wiremock.org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public final class UserController {

    private final UserDetailService userDetailService;

    public UserController(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    public static List<UserDTO> users = new ArrayList<>();

    @GetMapping("/users")
    public List<UserDTO> getAll(@RequestParam(value = "name", required = false) String name) {
        if (StringUtils.isNotBlank(name)) {
            return users.stream().filter(u -> u.getFirstName().toLowerCase().contains(name.toLowerCase())
                    || u.getLastName().toLowerCase().contains(name.toLowerCase()))
                    .collect(Collectors.toList());
        }
        return users;
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDTO> get(@PathVariable("id") String id, @RequestParam(required = false) String format) {
        UserDTO currentUser = users.stream().filter(u -> u.getId()
                .equals(id)).findFirst()
                .orElse(null);
        if (currentUser == null) {
            return ResponseEntity
                    .notFound().build();
        }

        ResponseEntity<UserDetailsDTO> responseUserDetails = userDetailService.getUserDetails(currentUser.getId(), format);
        Assert.assertNotNull(responseUserDetails);
        if (!responseUserDetails.getStatusCode().is2xxSuccessful()) {
            return ResponseEntity.badRequest()
                    .build();
        }
        currentUser.setDetails(responseUserDetails.getBody());

        return ResponseEntity.
                ok()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(currentUser);
    }

    @PostMapping(value = "/users")
    public ResponseEntity<UserDTO> addUser(@RequestBody  UserDTO user) {

        UserDTO currentUser = users.stream().filter(u -> u.getId()
                .equals(user.getId())).findFirst()
                .orElse(null);
        if (currentUser == null) {
            users.add(user);
            return ResponseEntity.status(201)
                    .body(user);
        }
        return ResponseEntity.
                badRequest()
                .build();

    }

    @PostMapping(value = "/users", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<List<UserDTO>> uploadFile(@RequestParam(name = "file") MultipartFile file) throws IOException {

        CsvToBean<UserCsvLine> csvBean = new CsvToBeanBuilder<UserCsvLine>(new InputStreamReader(file.getInputStream()))
                .withType(UserCsvLine.class)  // Convert a csv string line to PaymentIdentityAuditImportCsvLine
                .withIgnoreLeadingWhiteSpace(true) // White space in front of a quote in a field is ignored
                .withSeparator(';')
                .build();

        Assert.assertNotNull(csvBean);
        List<UserDTO> csvUsers = csvBean.parse()
                .stream()
                .map(userCsvLine -> {
                    UserDTO userDTO = new UserDTO();
                    userDTO.setId(userCsvLine.getId());
                    userDTO.setAge(userCsvLine.getAge());
                    userDTO.setFirstName(userCsvLine.getFirstName());
                    userDTO.setLastName(userCsvLine.getLastName());

                    return userDTO;
                })
                .collect(Collectors.toList());

        users.addAll(csvUsers);

        return ResponseEntity.ok(csvUsers);
    }

    @PutMapping(value = "/users/{id}")
    public ResponseEntity<UserDTO> updateUser(@RequestBody  UserDTO user, @PathVariable String id) {

        UserDTO currentUser = users.stream().filter(u -> u.getId()
                .equals(id)).findFirst()
                .orElse(null);
        if (currentUser == null) {
            return ResponseEntity
                    .notFound().build();
        }

        users.stream().filter(u -> id.equals(u.getId())).forEach(u -> {
            u.setFirstName(user.getFirstName());
            u.setLastName(user.getLastName());
            u.setAge(user.getAge());
            u.setRelatedTo(user.getRelatedTo());
        });

        return ResponseEntity.
                ok(user);
    }

    @PatchMapping(value = "/users/{id}")
    public ResponseEntity<UserDTO> patchUser(@RequestBody  PartialUserDTO user, @PathVariable String id) {

        UserDTO currentUser = users.stream().filter(u -> u.getId()
                .equals(id)).findFirst()
                .orElse(null);
        if (currentUser == null) {
            return ResponseEntity
                    .notFound().build();
        }

        currentUser.setLastName(user.getLastName());

        users.stream().filter(u -> id.equals(u.getId()))
                .forEach(u -> u.setLastName(user.getLastName()));

        return ResponseEntity.
                ok(currentUser);
    }

    @DeleteMapping(value = "/users/{id}")
    public ResponseEntity<UserDTO> deleteUser(@PathVariable("id") String id) {

        UserDTO currentUser = users.stream().filter(u -> u.getId()
                .equals(id)).findFirst()
                .orElse(null);
        if (currentUser == null) {
            return ResponseEntity
                    .notFound().build();
        }

        users = users.stream().filter(u -> !currentUser.equals(u))
                .collect(Collectors.toList());

        return ResponseEntity.
                ok().build();

    }

    @RequestMapping(value = "/authenticated", method = RequestMethod.HEAD)
    public ResponseEntity<Void> authenticated(HttpServletRequest request) {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        boolean isAuthenticated = StringUtils.isNotBlank(authHeader);
        if (isAuthenticated) {
            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, authHeader)
                    .build();
        }
        return ResponseEntity.status(401).build();
    }
}
