public class TestPlanet
{
    public static void main(String[] args)
    {
        Planet p1 = new Planet(1.0, 1.0, 3.0, 4.0, 5e12, "jupiter.gif");
        Planet p2 = new Planet(2.0, 3.0, 3.0, 4.0, 3e12, "jupiter.gif");
        System.out.printf("Force between them is %f,%f",p1.calcForceExertedByX(p2),p1.calcForceExertedByY(p2));
    }
}