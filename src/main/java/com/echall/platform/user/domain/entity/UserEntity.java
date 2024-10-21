package com.echall.platform.user.domain.entity;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.DynamicUpdate;

import com.echall.platform.bookmark.domain.entity.BookmarkEntity;
import com.echall.platform.oauth2.domain.info.OAuth2UserPrincipal;
import com.echall.platform.scrap.domain.entity.ScrapEntity;
import com.echall.platform.user.domain.dto.UserRequestDto;
import com.echall.platform.user.domain.enums.Gender;
import com.echall.platform.user.domain.enums.Role;
import com.echall.platform.user.domain.enums.UserStatus;
import com.echall.platform.util.RandomNicknameGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "user")
@DynamicUpdate
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, columnDefinition = "varchar(255)")
	private String username;

	@Column(nullable = false, columnDefinition = "varchar(255)")
	private String nickname;

	@Column(columnDefinition = "varchar(255)")
	private String password;

	@Column(nullable = false, unique = true, columnDefinition = "varchar(255)")
	private String email;

	@Column(name = "phone_number", columnDefinition = "varchar(255)")
	private String phoneNumber;

	@Column(columnDefinition = "date")
	private LocalDate birth;

	@Enumerated(EnumType.STRING)
	private Gender gender;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private Role role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, name = "user_status")
	private UserStatus userStatus;

	@Column(nullable = false, columnDefinition = "varchar(255)")
	private String provider;

	@Column(nullable = false, columnDefinition = "varchar(255)")
	private String providerId;

	@OneToMany
	@JoinColumn(name = "user_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private List<BookmarkEntity> bookmarks = new ArrayList<>();

	@OneToMany
	@JoinColumn(name = "user_id", foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
	private List<ScrapEntity> scraps = new ArrayList<>();

	// For Spring Security==============================================================================================
	@Builder
	public UserEntity(String username, String nickname, String password, String email, String phoneNumber,
		LocalDate birth, Gender gender, Role role, UserStatus userStatus, String provider, String providerId) {
		this.username = username;
		this.nickname = nickname;
		this.password = password;
		this.email = email;
		this.phoneNumber = phoneNumber;
		this.birth = birth;
		this.gender = gender;
		this.role = role;
		this.userStatus = userStatus;
		this.provider = provider;
		this.providerId = providerId;
	}

	public static UserEntity createByOAuthUser(OAuth2UserPrincipal oAuthUser) {
		return UserEntity.builder()
			.username(oAuthUser.getUsername())
			.nickname(RandomNicknameGenerator.setRandomNickname())
			.email(oAuthUser.getEmail())
			.role(Role.ROLE_USER)
			.userStatus(UserStatus.USER_STATUS_CREATED)
			.provider(oAuthUser.getProvider())
			.providerId(oAuthUser.getProviderId())
			.build();
	}

	public void setUserInitialInfo(UserRequestDto.UserUpdateRequest userUpdateRequest) {
		this.username = userUpdateRequest.username();
		this.nickname = userUpdateRequest.nickname();
		this.phoneNumber = userUpdateRequest.phoneNumber();
		this.birth = userUpdateRequest.birth();
		this.gender = userUpdateRequest.gender();
		this.userStatus = UserStatus.USER_STATUS_ACTIVATE;
	}

	public void updateUserInfo(UserRequestDto.UserUpdateRequest userUpdateRequest) {
		this.username = userUpdateRequest.username() == null ? this.username : userUpdateRequest.username();
		this.nickname = userUpdateRequest.nickname() == null ? this.nickname : userUpdateRequest.nickname();
		this.phoneNumber = userUpdateRequest.phoneNumber() == null ? this.phoneNumber : userUpdateRequest.phoneNumber();
		this.birth = userUpdateRequest.birth() == null ? this.birth : userUpdateRequest.birth();
	}

	/*
	user.updateUserBookmark() 메서드입니다!
	 */
	public void updateUserBookmark(BookmarkEntity bookmark) {
		this.bookmarks.add(bookmark);
	}

	public void updateAfterOAuth2Login(OAuth2UserPrincipal oAuthUser) {
		this.username = oAuthUser.getUsername();
		this.provider = oAuthUser.getProvider();
		this.providerId = oAuthUser.getProviderId();
	}

	public boolean hasContent(Long contentId){
		return this.scraps.stream()
			.anyMatch(scrap -> scrap.getContent().getId().equals(contentId));
	}
}
