public class NBody {
    public static double readRadius(String adr)
    {
        In in = new In(adr);
        in.readInt();
        return in.readDouble();
    }
    public static Planet[] readPlanets(String adr)
    {
        In in = new In(adr);
        int n = in.readInt();
        Planet[] ps = new Planet[n];
        double x,y,vx,vy,m;
        String pic;
        in.readDouble();
        for(int i=0;i<n;i++)
        {
            x = in.readDouble();
            y = in.readDouble();
            vx = in.readDouble();
            vy = in.readDouble();
            m = in.readDouble();
            pic = in.readString();
            ps[i] = new Planet(x,y,vx,vy,m,pic);
        }
        return ps;
    }
    public static void main(String[] args)
    {
        double T = Double.parseDouble(args[0]);
        double dt = Double.parseDouble(args[1]);
        String filename = args[2];
        double radius = readRadius(filename);
        Planet[] planets = readPlanets(filename);
        StdDraw.setScale(-radius,radius);
        StdDraw.clear();
        StdDraw.picture(0,0,"images/starfield.jpg");
        //StdAudio.play("audio/2001.mid");
        for(int i=0;i<planets.length;i++)
        {
            planets[i].draw();
        }
        double[] xForces = new double[planets.length];
        double[] yForces = new double[planets.length];
        for(double t = 0;t<T;t+=dt)
        {
            for(int i=0;i<planets.length;i++)
            {
                xForces[i] = planets[i].calcNetForceExertedByX(planets);
                yForces[i] = planets[i].calcNetForceExertedByY(planets);
            }
            for(int i=0;i<planets.length;i++)
            {
                planets[i].update(dt,xForces[i],yForces[i]);
            }
            StdDraw.clear();
            StdDraw.picture(0,0,"images/starfield.jpg");
            for(int i=0;i<planets.length;i++)
            {
                planets[i].draw();
            }
            StdDraw.show(10);
        }
        StdOut.printf("%d\n", planets.length);
        StdOut.printf("%.2e\n", radius);
        for (int i = 0; i < planets.length; i++)
        {
            StdOut.printf("%11.4e %11.4e %11.4e %11.4e %11.4e %12s\n",
                planets[i].xxPos, planets[i].yyPos, planets[i].xxVel, planets[i].yyVel, planets[i].mass, planets[i].imgFileName);   
        }   
    }
}
