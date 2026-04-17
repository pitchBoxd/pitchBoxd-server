#!/bin/bash

# [설정] 색상
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

echo -e "${GREEN}>>> 1. 환경 설정 및 경로 분석${NC}"

# 1. 경로 및 파일 설정
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
SOURCE_FILE=$1
RULE_FILE="$SCRIPT_DIR/test-rules.md"

if [ -z "$SOURCE_FILE" ]; then
    echo -e "${RED}❌ 오류: 대상 소스 파일 경로를 입력해주세요.${NC}"
    exit 1
fi

# 2. 경로 계산
TARGET_PATH=${SOURCE_FILE/src\/main/src\/test}
OUTPUT_FILE="${TARGET_PATH%.java}Test.java"
CLASS_NAME=$(basename "$OUTPUT_FILE" .java)
TARGET_DIR=$(dirname "$OUTPUT_FILE")
BACKUP_FILE="${OUTPUT_FILE}.bak"

echo "대상 소스: $SOURCE_FILE"
echo "생성 위치: $OUTPUT_FILE"

# 3. 기존 테스트 백업
EXISTING_CONTEXT=""
if [ -f "$OUTPUT_FILE" ]; then
    echo -e "${YELLOW}⚠️ 기존 테스트 발견: 백업을 생성합니다.${NC}"
    cp "$OUTPUT_FILE" "$BACKUP_FILE"
    EXISTING_CONTEXT="\n### Existing Test Code\n$(cat "$OUTPUT_FILE")"
fi

echo -e "\n${GREEN}>>> 2. Gemini CLI를 통한 코드 생성 및 정제${NC}"
mkdir -p "$TARGET_DIR"

# 4. 생성 및 코드 블록 제거
# 백틱 세 개를 변수에 담아 쉘의 오해를 방지함
TICK='```'

(
  cat "$RULE_FILE"
  echo -e "$EXISTING_CONTEXT"
  echo -e "\n### Source Code to Test\n"
  cat "$SOURCE_FILE"
) | gemini > "${OUTPUT_FILE}.tmp"

# grep -F 를 사용해 변수에 담긴 백틱 문자열이 포함된 줄을 제외함
grep -v "$TICK" "${OUTPUT_FILE}.tmp" > "$OUTPUT_FILE"
rm "${OUTPUT_FILE}.tmp"

echo "✅ 정제 완료 (코드 블록 기호 제거됨)"

echo -e "\n${GREEN}>>> 3. 빌드 도구 기반 테스트 실행${NC}"

# 5. 테스트 실행 함수
run_test() {
    if [ -f "./gradlew" ]; then
        ./gradlew test --tests "$CLASS_NAME"
        return $?
    elif [ -f "./mvnw" ]; then
        ./mvnw test -Dtest="$CLASS_NAME"
        return $?
    else
        echo "❌ 빌드 도구를 찾을 수 없습니다."
        return 0
    fi
}

run_test
TEST_RESULT=$?

# 6. 사후 처리
if [ $TEST_RESULT -eq 0 ]; then
    echo -e "\n${GREEN}✨ 테스트 성공! .bak 삭제${NC}"
    [ -f "$BACKUP_FILE" ] && rm "$BACKUP_FILE"
else
    echo -e "\n${RED}❌ 테스트 실패!${NC}"
    exit 1
fi