package com.open.spring.mvc.grades;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class GradeTest {

    @Test
    public void testGradeFields() {
        Grade grade = new Grade();
        String uid = "gh_12345";

        // Test uid setter/getter
        grade.setUid(uid);
        assertEquals(uid, grade.getUid(), "getUid should return set uid");

        // Test teacherComments field
        String teacherComments = "CSA";
        grade.setTeacherComments(teacherComments);
        assertEquals(teacherComments, grade.getTeacherComments(), "getTeacherComments should return set teacherComments");
    }
}
