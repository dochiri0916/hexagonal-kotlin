value class를 쓰는 이유

- 런타입 비용 제로: 컴파일 시 내부 값(String)으로 치환되어 객체 할당 없음
- 타입 안정성 유지: 컴파일 타임에는 Email과 String이 구분되어 실수 방지
- 보일러플레이트 제거: equals, hashCode, toString 자동 위임
- private constructor: from() 팩토리를 강제하여 검증 우회 불가