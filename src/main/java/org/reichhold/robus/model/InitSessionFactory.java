package org.reichhold.robus.model;

/**
 * Created with IntelliJ IDEA.
 * User: matthias
 * Date: 26.07.12
 * Time: 14:57
 * To change this template use File | Settings | File Templates.
 */
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class InitSessionFactory {
    /** The single instance of hibernate SessionFactory */
    private static SessionFactory sessionFactory;

    private InitSessionFactory() {
    }

    static {
        final Configuration cfg = new Configuration();
        //cfg.configure("/src/hibernate.cfg.xml");
        cfg.configure("/hibernate.cfg.xml");
        sessionFactory = cfg.buildSessionFactory();
    }

    public static SessionFactory getInstance() {
        /*sessionFactory = new Configuration()
                .configure() // configures settings from hibernate.cfg.xml
                .buildSessionFactory();*/

        return sessionFactory;
    }
}