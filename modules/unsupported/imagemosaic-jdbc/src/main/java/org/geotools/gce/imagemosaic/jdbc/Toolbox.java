package org.geotools.gce.imagemosaic.jdbc;

class Toolbox {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Missing cmd import | ddl");
            System.exit(1);
        }

        String[] newArgs = new String[args.length - 1];

        for (int i = 0; i < newArgs.length; i++)
            newArgs[i] = args[i + 1];

        if ("import".equalsIgnoreCase(args[0])) {
            Import.start(newArgs);
        } else if ("ddl".equalsIgnoreCase(args[0])) {
            DDLGenerator.start(newArgs);
        } else {
            System.out.println("Unknwon cmd: " + args[0]);
            System.exit(1);
        }
    }
}
