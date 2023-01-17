package com.global.domain;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter @Setter @EqualsAndHashCode(of = "id")
@Builder @AllArgsConstructor @NoArgsConstructor
public class Account {

  @Id  @GeneratedValue
  private Long id;

  @Column(unique = true)
  private String email;

  @Column(unique = true)
  private String nickName;

  private String password;
  private boolean emailVerified;
  private String emailCheckToken;
  private LocalDateTime joinedAt;
  private String bio;
  private String url;
  private String occupation;
  private String location;

  @Lob @Basic(fetch=FetchType.EAGER)
  private String profileImage;

  private boolean studyCreateByEmail;

  private boolean studyCreateByWeb;

  private boolean studyEnrollmentResultByEmail;

  private boolean studyEnrollmentResultByWeb;

  private boolean studyUpdateByEmail;

  private boolean studyUpdateByWeb;

  public void generateEmailCheckToken() {
    // UUID.randomUUID()
    //       ㄴ random 한 UUID 생성
    // UUID (Universally Unique Identifier)
    // ㄴ 네트워크 상에서의 ID 의 고유성을 보장하는 규약
    this.emailCheckToken = UUID.randomUUID().toString();
  }
}
