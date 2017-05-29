package cn.emac.demo.spring5.reactive.domain;

import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.persistence.Id;

/**
 * @author Emac
 * @since 2017-05-29
 */
@Document
@Data
@NoArgsConstructor
@RequiredArgsConstructor
public class Restaurant {

    @Id
    public String id;
    @NonNull
    private String name;
    @NonNull
    private String address;
    @NonNull
    private String telephone;
}
