#!/bin/bash

echo "=========================================="
echo "CONSERVATIVE DELETION - 100% SAFE ONLY"
echo "=========================================="
echo ""

DELETED_COUNT=0

# 1. DELETE USER ENTITY
if [ -d "src/main/java/com/open/spring/mvc/user" ]; then
    echo "Deleting User entity..."
    rm -rf src/main/java/com/open/spring/mvc/user/
    ((DELETED_COUNT++))
    echo "   DELETED: User.java, UserApiController.java, UserJpaRepository.java"
fi

# 2. DELETE STUDENT RESPONSE ENTITIES
if [ -f "src/main/java/com/open/spring/mvc/assignments/StudentResponse.java" ]; then
    echo "Deleting StudentResponse entities..."
    rm -f src/main/java/com/open/spring/mvc/assignments/StudentResponse.java
    rm -f src/main/java/com/open/spring/mvc/assignments/StudentResponseController.java
    rm -f src/main/java/com/open/spring/mvc/assignments/StudentResponseRepository.java
    ((DELETED_COUNT++))
    echo "   DELETED: StudentResponse.java, StudentResponseController.java, StudentResponseRepository.java"
fi

# 3. DELETE STUDENT ENTITY (teamteach)
if [ -f "src/main/java/com/open/spring/mvc/teamteach/Student.java" ]; then
    echo "Deleting Student.java from teamteach..."
    rm -f src/main/java/com/open/spring/mvc/teamteach/Student.java
    ((DELETED_COUNT++))
    echo "   DELETED: teamteach/Student.java"
fi

# 4. DELETE PERSON USER MAPPING
MAPPING_FILES=$(find src/main/java -name "*PersonUserMapping*" 2>/dev/null)
if [ -n "$MAPPING_FILES" ]; then
    echo "Deleting PersonUserMapping entities..."
    find src/main/java -name "*PersonUserMapping*" -delete
    ((DELETED_COUNT++))
    echo "   DELETED: PersonUserMapping files"
fi

echo ""
echo "=========================================="
echo "Entity Cleanup Complete"
echo "Deleted: $DELETED_COUNT entity packages"
echo ""
echo "Next steps:"
echo "1. Verify build: ./mvnw clean compile"
echo "2. Add table drops to ModelInit.java"
echo "3. Test server startup"
echo "=========================================="
