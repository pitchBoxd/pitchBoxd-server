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

# 3. 기존 테스트 백업 및 컨텍스트 준비
EXISTING_CONTEXT=""
if [ -f "$OUTPUT_FILE" ]; then
    echo -e "${YELLOW}⚠️ 기존 테스트 발견: 백업을 생성합니다.${NC}"
    cp "$OUTPUT_FILE" "$BACKUP_FILE"
    # 기존 코드에서 패키지 선언부만 남기고 노이즈가 섞이지 않도록 함
    EXISTING_CONTEXT="\n### Existing Test Code\n$(cat "$OUTPUT_FILE")"
fi

echo -e "\n${GREEN}>>> 2. Gemini CLI를 통한 코드 생성 및 정제${NC}"
mkdir -p "$TARGET_DIR"

# 4. 생성 및 코드 블록/메타데이터 제거
TICK='```'

(
  cat "$RULE_FILE"
  echo -e "$EXISTING_CONTEXT"
  echo -e "\n### Source Code to Test\n"
  cat "$SOURCE_FILE"
) | gemini > "${OUTPUT_FILE}.tmp"

# [핵심 수정 부분]
# 1. 백틱(```) 제거
# 2. 'package ' 문구가 나오는 줄부터 파일 끝까지 추출 (상단에 붙는 파일 경로/설명 제거)
grep -v "$TICK" "${OUTPUT_FILE}.tmp" | sed -n '/package /,$p' > "$OUTPUT_FILE"

rm "${OUTPUT_FILE}.tmp"

# 파일이 비어있는지 체크 (정제 과정에서 오류 발생 시 대응)
if [ ! -s "$OUTPUT_FILE" ]; then
    echo -e "${RED}❌ 오류: 코드 정제 실패. 생성된 파일이 비어있습니다.${NC}"
    [ -f "$BACKUP_FILE" ] && mv "$BACKUP_FILE" "$OUTPUT_FILE"
    exit 1
fi

echo "✅ 정제 완료 (코드 블록 및 상단 메타데이터 제거됨)"

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
        echo -e "${RED}❌ 빌드 도구를 찾을 수 없습니다.${NC}"
        return 1
    fi
}

run_test
TEST_RESULT=$?

# 6. 사후 처리
if [ $TEST_RESULT -eq 0 ]; then
    echo -e "\n${GREEN}✨ 테스트 성공! .bak 삭제${NC}"
    [ -f "$BACKUP_FILE" ] && rm "$BACKUP_FILE"
else
    echo -e "\n${RED}❌ 테스트 실패! 이전 상태로 복구합니다.${NC}"
    if [ -f "$BACKUP_FILE" ]; then
        mv "$BACKUP_FILE" "$OUTPUT_FILE"
        echo "🔙 백업 파일로 복구 완료."
    fi
    exit 1
fi