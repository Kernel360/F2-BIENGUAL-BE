# <img src="https://github.com/user-attachments/assets/d0d20a88-bb21-45fe-8071-9dba686115a3" alt="biengual-favicon" width="30" height="30"> Biengual
- 영어 학습 플랫폼 : https://biengual.store
- '2개국어능통자'라는 Bilingual이라는 단어에서 착안하여, 홀로 영어공부학습을 시도하는 사람들에게 '2가지' 학습방법(리스닝,리딩)을 통해 '영어능통자'가 되기 위한 여정을 함께하는 영어학습플랫폼
- 프론트 Github : https://github.com/Kernel360/F2-BIENGUAL-FE

## 🎯 기획의도

- 목표
    - 사용자의 관심사에 맞춘 실생활 콘텐츠를 통해 리딩과 리스닝 능력 향상
    - CNN기사와 유튜브 영상 콘텐츠의 관심있는 주제 콘텐츠 학습

## ❓문제상황

1. 인터넷에 있는 영어 콘텐츠들을 학습하기 위해서는 바로 기록할 수 있는 것이 아니라, 메모 플랫폼에 따로 기록해야 하는 번거로움 존재.
2. 홀로 영어공부를 하는 사람을 위한 실력 검증 과정 부족
3. 영상을 공부하면서 학습 진행상황을 학습자가 스스로 기록해야하는 번거로움
4. 관심사에 맞는 콘텐츠 제공

## 💡 해결방법

1. Ebook처럼 기사, 영상 스크립트에 문장 단위로 하이라이팅(북마크), 필기(메모) 가능
2. 퀴즈를 통한 개별 콘텐츠 복습 기능
3. 시각화된 대시보드로 학습 진행 상황 확인 가능
4. 다양한 유튜브 채널 및 CNN 의 이슈되는 기사들을 크롤링 하여 다양한 카테고리의 콘텐츠를 가져오고
이를 기반으로 추천 시스템을 통해 여러 콘텐츠를 추천

## ⭐ 주요기능

- 소셜 로그인
    - Kakao
    - Naver
    - Google
- 크롤링
    - Youtube Data API - Youtube 정보
    - Selenium - Youtube 자막 동적 크롤링
    - JSoup - CNN 기사 정적 크롤링
- 문제 생성
    - Perplexity AI API 를 이용한 문제, 답, 힌트 생성
- 카테고리
    - 크롤링 해 온 카테고리가 없으면 엔티티 추가하는 방식으로 생성
    - NLP(Word2Vec) 을 이용하여 유사한 카테고리 분류
- 번역
    - Microsoft Azure AI API
- 북마크
- 스크랩
- 이미지 로드
    - S3 업로드
        - 로컬 환경의 경우 LocalStack 활용
    - CDN 을 활용한 이미지 로드 속도 개선
- 추천 시스템
    - 카테고리 추천 시스템 : User-Based Collaborative Filtering 기반 추천 시스템
    - 북마크 추천 시스템 : 단순 중복 북마크 개수 기반 정렬(인기순)
- 검색
    - OpenSearch를 이용해 키워드에 해당하는 콘텐츠를 검색할 수 있습니다.
    - 검색 내용은 제목, 카테고리, 본문 포함
    - Fuzzy Query 및 내부 로직(레벤슈타인 거리 알고리즘) 을 이용해 약간의 오타여도 정상적인 검색이 수행 가능
- 대시보드
    - 히스토리 테이블 기반하여 학습 데이터 통계 보여줌
    - 최근 학습 기록, 한 달 미션 기록, 5주 퀴즈 정답율 기록
    - 현 포인트 및 한 달 포인트 사용 내역, 카테고리 별 학습 비율 기록
- 분산락
    - Reddison 락
        - 콘텐츠 난이도 피드백
        - 학습률 업데이트
        - 최신 콘텐츠 포인트 지불
- 캐시 (Lettuce)
    - 카테고리 추천 시스템 기록 읽어올 때 (ttl : 10분)
- 스케줄러
    - 일 단위 : 일일 미션 기록 리셋, 콘텐츠 피드백 데이터 합산, CNN 및 유튜브 크롤링
    - 주 단위 : 북마크 추천 시스템 업데이트

## ERD
- [ERD](https://dbdiagram.io/d/Copy-of-LMS-670dcf7397a66db9a3f9b8c4)
    - MySQL : 전반적인 데이터 저장
    - Mongo : 스크립트 및 문제 관련 내용 저장
    - Redis : 캐시
    ![Copy of LMS (1)](https://github.com/user-attachments/assets/dbd6171e-58d8-4969-a3c6-3459e6a79a12)

## 데이터 흐름 및 시스템 구조도
![Group 78](https://github.com/user-attachments/assets/3ad9481d-0ef9-403e-a2b2-8800cdad7cdd)

## 기술 스택
 - Java 17
 - Spring Boot 3.3.3
 - Spring Security 6
 - JWT, Cookie
 - OAuth2 (Google, Naver, Kakao)
 - JPA, QueryDsl
 - MySQL, MongoDB, Redis
 - Flyway
 - Swagger
 - Jsoup, Selenium
   - Chrome Driver, Xvfb
 - Rest API
 - AOP
 - ELK
 - EC2, GitHub Actions, Docker, Nginx
 - Open Search
 - S3, LocalStack, CDN
