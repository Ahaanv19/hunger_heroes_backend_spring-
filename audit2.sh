#!/bin/bash

echo "=========================================="
echo "PRE-DELETION DATABASE AUDIT"
echo "=========================================="
echo ""

# ==========================================
# 1. USER ENTITY
# ==========================================
echo "1. USER ENTITY"
echo "----------------------------------------"
USER_ROWS=$(sqlite3 volumes/sqlite.db "SELECT COUNT(*) FROM user;" 2>/dev/null)
echo "   Database rows: $USER_ROWS"

USER_IMPORTS=$(grep -r "import.*\.user\.User[^A-Za-z]" src/main/java/ --include="*.java" 2>/dev/null | grep -v "UserDetails" | grep -v "UserJpa" | wc -l | xargs)
echo "   Code imports: $USER_IMPORTS"

USER_AUTOWIRED=$(grep -r "@Autowired" src/main/java/ -A 1 --include="*.java" 2>/dev/null | grep "UserJpaRepository" | wc -l | xargs)
echo "   Autowired instances: $USER_AUTOWIRED"

USER_FRONTEND=$(find src/main/resources -name "*.html" -o -name "*.js" 2>/dev/null | xargs grep -l "/user/" 2>/dev/null | wc -l | xargs)
echo "   Frontend references: $USER_FRONTEND"

if [ "$USER_ROWS" -eq 0 ] && [ "$USER_IMPORTS" -eq 0 ] && [ "$USER_AUTOWIRED" -eq 0 ] && [ "$USER_FRONTEND" -eq 0 ]; then
    echo "   VERDICT: SAFE TO DELETE"
else
    echo "   VERDICT: POTENTIALLY IN USE - INVESTIGATE"
fi
echo ""

# ==========================================
# 2. STUDENT RESPONSE (responses table)
# ==========================================
echo "2. STUDENT RESPONSE / RESPONSES TABLE"
echo "----------------------------------------"
RESPONSES_ROWS=$(sqlite3 volumes/sqlite.db "SELECT COUNT(*) FROM responses;" 2>/dev/null)
echo "   Database rows: $RESPONSES_ROWS"

RESPONSE_FILES=$(find src/main/java -name "*StudentResponse*" 2>/dev/null | wc -l | xargs)
echo "   Java files: $RESPONSE_FILES"

RESPONSE_IMPORTS=$(grep -r "StudentResponse" src/main/java/ --include="*.java" 2>/dev/null | grep "import" | wc -l | xargs)
echo "   Code imports: $RESPONSE_IMPORTS"

RESPONSE_FRONTEND=$(find src/main/resources -name "*.html" -o -name "*.js" 2>/dev/null | xargs grep -l "responses" 2>/dev/null | wc -l | xargs)
echo "   Frontend references: $RESPONSE_FRONTEND"

if [ "$RESPONSES_ROWS" -eq 0 ] && [ "$RESPONSE_IMPORTS" -eq 0 ]; then
    echo "   VERDICT: SAFE TO DELETE"
else
    echo "   VERDICT: POTENTIALLY IN USE - INVESTIGATE"
fi
echo ""

# ==========================================
# 3. STUDENT ENTITY (teamteach/Student.java)
# ==========================================
echo "3. STUDENT ENTITY"
echo "----------------------------------------"
STUDENT_ROWS=$(sqlite3 volumes/sqlite.db "SELECT COUNT(*) FROM student;" 2>/dev/null)
echo "   Database rows: $STUDENT_ROWS"

STUDENT_FILE=$(find src/main/java -path "*/teamteach/Student.java" 2>/dev/null)
if [ -n "$STUDENT_FILE" ]; then
    echo "   Entity file exists: YES"
    STUDENT_IMPORTS=$(grep -r "import.*teamteach\.Student" src/main/java/ --include="*.java" 2>/dev/null | wc -l | xargs)
    echo "   Code imports: $STUDENT_IMPORTS"
else
    echo "   Entity file exists: NO"
    STUDENT_IMPORTS=0
fi

if [ "$STUDENT_ROWS" -eq 0 ] && [ "$STUDENT_IMPORTS" -eq 0 ]; then
    echo "   VERDICT: SAFE TO DELETE"
else
    echo "   VERDICT: POTENTIALLY IN USE - INVESTIGATE"
fi
echo ""

# ==========================================
# 4. PERSON USER MAPPING
# ==========================================
echo "4. PERSON USER MAPPING"
echo "----------------------------------------"
MAPPING_ROWS=$(sqlite3 volumes/sqlite.db "SELECT COUNT(*) FROM person_user_mapping;" 2>/dev/null)
echo "   Database rows: $MAPPING_ROWS"

MAPPING_FILES=$(find src/main/java -name "*PersonUserMapping*" 2>/dev/null | wc -l | xargs)
echo "   Java files: $MAPPING_FILES"

MAPPING_IMPORTS=$(grep -r "PersonUserMapping" src/main/java/ --include="*.java" 2>/dev/null | grep "import" | wc -l | xargs)
echo "   Code imports: $MAPPING_IMPORTS"

if [ "$MAPPING_ROWS" -eq 0 ] && [ "$MAPPING_IMPORTS" -eq 0 ]; then
    echo "   VERDICT: SAFE TO DELETE"
else
    echo "   VERDICT: POTENTIALLY IN USE - INVESTIGATE"
fi
echo ""

# ==========================================
# 5. PROGRESS TABLE
# ==========================================
echo "5. PROGRESS TABLE"
echo "----------------------------------------"
PROGRESS_ROWS=$(sqlite3 volumes/sqlite.db "SELECT COUNT(*) FROM progress;" 2>/dev/null)
echo "   Database rows: $PROGRESS_ROWS"

PROGRESS_FILES=$(find src/main/java/com/open/spring/mvc -name "*Progress.java" ! -name "*ProgressBar*" 2>/dev/null | wc -l | xargs)
echo "   Java files (excluding ProgressBar): $PROGRESS_FILES"

PROGRESS_IMPORTS=$(grep -r "import.*\.Progress[^B]" src/main/java/ --include="*.java" 2>/dev/null | wc -l | xargs)
echo "   Code imports: $PROGRESS_IMPORTS"

PROGRESS_FRONTEND=$(find src/main/resources -name "*.html" -o -name "*.js" 2>/dev/null | xargs grep -l "/api/progress" 2>/dev/null | wc -l | xargs)
echo "   Frontend API references: $PROGRESS_FRONTEND"

if [ "$PROGRESS_ROWS" -gt 0 ]; then
    echo "   WARNING: HAS DATA ($PROGRESS_ROWS rows)"
    echo "   Sample data:"
    sqlite3 volumes/sqlite.db "SELECT * FROM progress LIMIT 3;" 2>/dev/null | head -3
else
    echo "   VERDICT: SAFE TO DELETE (no data)"
fi
echo ""

# ==========================================
# 6. ASSIGNMENT QUEUE TABLE
# ==========================================
echo "6. ASSIGNMENT QUEUE TABLE"
echo "----------------------------------------"
QUEUE_ROWS=$(sqlite3 volumes/sqlite.db "SELECT COUNT(*) FROM assignment_queue;" 2>/dev/null)
echo "   Database rows: $QUEUE_ROWS"

QUEUE_CONVERTER=$(grep -r "AssignmentQueueConverter" src/main/java/ --include="*.java" 2>/dev/null | wc -l | xargs)
echo "   Uses JSON converter: $QUEUE_CONVERTER"

if [ "$QUEUE_ROWS" -eq 0 ]; then
    echo "   VERDICT: SAFE TO DELETE (already migrated to JSON)"
else
    echo "   WARNING: Table has $QUEUE_ROWS rows"
    echo "   Check if this is old data before migration"
fi
echo ""

# ==========================================
# 7. AUDIT TABLES (HTE_* and HT_*)
# ==========================================
echo "7. HIBERNATE ENVERS AUDIT TABLES"
echo "----------------------------------------"
AUDIT_TABLES=$(sqlite3 volumes/sqlite.db "SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND (name LIKE 'HTE_%' OR name LIKE 'HT_%');" 2>/dev/null)
echo "   Number of audit tables: $AUDIT_TABLES"

AUDITED_CODE=$(grep -r "@Audited" src/main/java/ --include="*.java" 2>/dev/null | wc -l | xargs)
echo "   @Audited annotations in code: $AUDITED_CODE"

if [ "$AUDITED_CODE" -eq 0 ]; then
    echo "   VERDICT: SAFE TO DELETE (orphaned audit tables)"
else
    echo "   WARNING: @Audited is still being used"
fi
echo ""

# ==========================================
# SUMMARY
# ==========================================
echo "=========================================="
echo "SUMMARY"
echo "=========================================="

SAFE_COUNT=0
UNSAFE_COUNT=0

# Check each entity
[ "$USER_ROWS" -eq 0 ] && [ "$USER_IMPORTS" -eq 0 ] && ((SAFE_COUNT++)) || ((UNSAFE_COUNT++))
[ "$RESPONSES_ROWS" -eq 0 ] && [ "$RESPONSE_IMPORTS" -eq 0 ] && ((SAFE_COUNT++)) || ((UNSAFE_COUNT++))
[ "$STUDENT_ROWS" -eq 0 ] && [ "$STUDENT_IMPORTS" -eq 0 ] && ((SAFE_COUNT++)) || ((UNSAFE_COUNT++))
[ "$MAPPING_ROWS" -eq 0 ] && [ "$MAPPING_IMPORTS" -eq 0 ] && ((SAFE_COUNT++)) || ((UNSAFE_COUNT++))
[ "$QUEUE_ROWS" -eq 0 ] && ((SAFE_COUNT++)) || ((UNSAFE_COUNT++))
[ "$AUDITED_CODE" -eq 0 ] && ((SAFE_COUNT++)) || ((UNSAFE_COUNT++))

echo "Safe to delete: $SAFE_COUNT entities"
echo "Need investigation: $UNSAFE_COUNT entities"
echo ""

if [ "$UNSAFE_COUNT" -eq 0 ]; then
    echo "VERDICT: All entities are safe to delete!"
else
    echo "VERDICT: Review entities marked 'POTENTIALLY IN USE'"
fi

echo ""
echo "=========================================="
