package sba.sms.services;

import sba.sms.dao.StudentI;
import sba.sms.models.Course;
import sba.sms.models.Student;
import org.hibernate.query.NativeQuery;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import sba.sms.utils.HibernateUtil;

import java.util.ArrayList;
import java.util.List;

public class StudentService implements StudentI {

    @Override
    public List<Student> getAllStudents() {
        Session s = HibernateUtil.getSessionFactory().openSession();
        List<Student> students = new ArrayList<>();

        try {
            students = s.createNativeQuery("SELECT * FROM student", Student.class).list();
        } catch (HibernateException exception) {
            exception.printStackTrace();
        } finally {
            s.close();
        }
        return students;
    }

    @Override
    public void createStudent(Student student) {

        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            s.persist(student);
            tx.commit();
        } catch (HibernateException exception) {
            if (tx != null) tx.rollback();
            exception.printStackTrace();
        }
    }

    @Override
    public Student getStudentByEmail(String email) {

        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Student student = s.get(Student.class, email);
            if (student == null) {
                throw new HibernateException("Did not find student");
            } else
                return student;
        } catch (HibernateException exception) {
            exception.printStackTrace();
        }
        return new Student();
    }

    @Override
    public boolean validateStudent(String email, String password) {

        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Student student = s.get(Student.class, email);
            if (student == null) {
                throw new HibernateException("Student is not registered");
            } else if (student.getEmail().equals(email) && student.getPassword().equals(password)) {
                return true;
            }
        } catch (HibernateException exception) {
            exception.printStackTrace();
        }
        return false;
    }

    @Override
    public void registerStudentToCourse(String email, int courseId) {

        Transaction tx = null;
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            Student student = s.get(Student.class, email);
            Course course = s.get(Course.class, courseId);
            course.addStudent(student);
            s.merge(course);
            tx.commit();
        } catch (HibernateException exception) {
            if (tx != null) tx.rollback();
            exception.printStackTrace();
        }
    }

    @Override
    public List<Course> getStudentCourses(String email) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = null;
        List<Course> courseList = null;

        try {
            tx = s.beginTransaction();
            NativeQuery<Course> query = s.createNativeQuery("SELECT c.id, c.name, c.instructor "
                    + "FROM course AS c JOIN student_courses AS sc ON c.id = sc.courses_id "
                    + "JOIN student as s ON sc.student_email = s.email WHERE email = :email", Course.class);
            query.setParameter("email", email);
            courseList = query.getResultList();
            tx.commit();
        } catch (HibernateException exception) {
            if (tx != null) {
                tx.rollback();
            }
            exception.printStackTrace();
        } finally {
            s.close();
        }
        return courseList;
    }
}
