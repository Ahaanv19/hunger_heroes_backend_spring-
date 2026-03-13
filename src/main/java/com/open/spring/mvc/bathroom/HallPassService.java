package com.open.spring.mvc.bathroom;

import java.sql.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HallPassService {

    @Autowired
    private TeacherJpaRepository teacherRepository;

    @Autowired
    private HallPassJpaRepository tinkleRepository;

    public Teacher getTeacherByName(String firstName, String lastName) {
        List<Teacher> teachers = teacherRepository.findByFirstnameIgnoreCaseAndLastnameIgnoreCase(firstName, lastName);
        return teachers.isEmpty() ? null : teachers.get(0); // Return first match
    }

    public HallPass getActivePassForUser(String username) {
        return tinkleRepository.findByPersonIdAndCheckoutIsNull(username).orElse(null);
    }

    public Teacher getTeacherForActivePass(HallPass pass) {
        return pass != null ? teacherRepository.findById(pass.getTeacher_id()).orElse(null) : null;
    }

    public HallPass requestPass(Long teacherId, int period, String activity, String email) {
        if (email != null) {
            HallPass pass = new HallPass();
            pass.setPersonId(email);
            pass.setTeacher_id(teacherId);
            pass.setCheckin(new Date(System.currentTimeMillis()));
            pass.setPeriod(period);
            pass.setActivity(activity);
            return tinkleRepository.save(pass);
        }
        return null;
    }

    public boolean checkoutPass(String email) {
        if (email != null) {
            Optional<HallPass> activePass = tinkleRepository.findByPersonIdAndCheckoutIsNull(email);
            if (activePass.isPresent()) {
                HallPass pass = activePass.get();
                pass.setCheckout(new Date(System.currentTimeMillis()));
                tinkleRepository.save(pass);
                return true;
            }
        }
        return false;
    }

    public Teacher getTeacherById(Long id) {
        return teacherRepository.findById(id).orElse(null);
    }

    /**
     * Adds a new teacher to the database.
     * @param teacher The Teacher object to be saved.
     * @return The saved Teacher object.
     */
    public Teacher addTeacher(Teacher teacher) {
        return teacherRepository.save(teacher);
    }

    /**
     * Removes a teacher by ID if they exist.
     * @param teacherId The ID of the teacher to remove.
     * @return True if successfully removed, False if teacher does not exist.
     */
    public boolean removeTeacher(Long teacherId) {
        Optional<Teacher> teacher = teacherRepository.findById(teacherId);
        if (teacher.isPresent()) {
            teacherRepository.deleteById(teacherId);
            return true;
        }
        return false;
    }
}

