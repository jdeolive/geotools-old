package org.openplans.filterfunctionwriter;

import java.io.PrintStream;
import java.lang.reflect.Method;

/**
 * Basic idea:
 * 
 * 1. for each method in the StaticGeometry class (or whatever class you specify -
 * see main() ) 2. make text desciption for it that you can stick in the service
 * meta-inf
 * 
 * @author dblasby
 */
public class MakeServicesInfo {

    public static void main(String[] args) {
        MakeServicesInfo cg = new MakeServicesInfo();

        cg.handleClass(org.geotools.filter.function.StaticGeometry.class); // parent
                                                                            // of
                                                                            // all
                                                                            // geometry
                                                                            // types
    }

    public void handleClass(Class c) {
        Method[] methods = c.getDeclaredMethods();
        for (int t = 0; t < methods.length; t++) {
            try {
                Method method = methods[t];
                PrintStream ps = System.out;

                // emitHeader(method,ps);
                emitCode(method, ps);
                // emitFooter(method,ps);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void emitCode(Method m, PrintStream printstream) {
        printstream.println("org.geotools.filter.function.FilterFunction_"
                + m.getName());
    }

}
