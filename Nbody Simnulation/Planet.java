public class Planet 
{
    public double xxPos;
    public double yyPos;
    public double xxVel;
    public double yyVel;
    public double mass;
    public String imgFileName;
    private static final double G = 0.0000000000667;
    public Planet(double xP, double yP, double xV,
              double yV, double m, String img){
        xxPos = xP;
        yyPos = yP;
        xxVel = xV;
        yyVel = yV;
        mass = m;
        imgFileName = img;
    }
    public Planet(Planet p){
        xxPos = p.xxPos;
        yyPos = p.yyPos;
        xxVel = p.xxVel;
        yyVel = p.yyVel;
        mass = p.mass;
        imgFileName = p.imgFileName;
    }
    private double calcDistanceX(Planet p)
    {
        return p.xxPos - xxPos;
    }
    private double calcDistanceY(Planet p)
    {
        return p.yyPos - yyPos;
    }
    public double calcDistance(Planet p)
    {
        double dx = calcDistanceX(p);
        double dy = calcDistanceY(p);
        return Math.sqrt(dx*dx+dy*dy);
    }
    public double calcForceExertedBy(Planet p)
    {
        double d = calcDistance(p);
        return G*mass*p.mass/(d*d);
    }
    public double calcForceExertedByX(Planet p)
    {
        double f = calcForceExertedBy(p);
        double d = calcDistance(p);
        double dx = calcDistanceX(p);
        return f*dx/d;
    }
    public double calcForceExertedByY(Planet p)
    {
        double f = calcForceExertedBy(p);
        double d = calcDistance(p);
        double dy = calcDistanceY(p);
        return f*dy/d;
    }
    public double calcNetForceExertedByX(Planet[] ps)
    {
        double netForce = 0;
        for(int i = 0;i<ps.length;i++)
        {
            if(!this.equals(ps[i]))
            {
                netForce += calcForceExertedByX(ps[i]);
            }
        }
        return netForce;
    }
    public double calcNetForceExertedByY(Planet[] ps)
    {
        double netForce = 0;
        for(int i = 0;i<ps.length;i++)
        {
            if(!this.equals(ps[i]))
            {
                netForce += calcForceExertedByY(ps[i]);
            }
        }
        return netForce;
    }
    public void update(double dt,double forceX,double forceY)
    {
        double ax = forceX/mass;
        double ay = forceY/mass;
        xxVel += dt*ax;
        yyVel += dt*ay;
        xxPos += dt*xxVel;
        yyPos += dt*yyVel;
    }
    public void draw()
    {
        StdDraw.picture(xxPos,yyPos,"images/"+imgFileName);
    }
}
