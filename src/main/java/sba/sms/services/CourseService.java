package sba.sms.services;

import sba.sms.dao.CourseI;
import sba.sms.models.Course;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import sba.sms.utils.HibernateUtil;

import java.util.List;

public class CourseService implements CourseI {

    @Override
    public void createCourse(Course course) {
        Transaction tx = null;

        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            tx = s.beginTransaction();
            s.persist(course);
            tx.commit();
        } catch (HibernateException exception) {
            if (tx != null) tx.rollback();
            exception.printStackTrace();
        }
    }

    @Override
    public Course getCourseById(int courseId) {
        try (Session s = HibernateUtil.getSessionFactory().openSession()) {
            Course course = s.get(Course.class, courseId);
            if (course == null)
                throw new HibernateException("Did not find course");
            else
                return course;
        } catch (HibernateException exception) {
            exception.printStackTrace();
        }
        return new Course();
    }

    @Override
    public List<Course> getAllCourses() {
        Session s = HibernateUtil.getSessionFactory().openSession();
        List<Course> courses = null;

        try {
            courses = s.createNativeQuery("SELECT * FROM course", Course.class).list();
        } catch (HibernateException exception) {
            exception.printStackTrace();
        } finally {
            s.close();
        }
        return courses;
    }
}
