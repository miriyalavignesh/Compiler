package cop5555sp15;
/** This class illustrates calling execute twice after modifying the int variable
* in the codelet. The expected output is
first time
0
2
second time
*/
public class Example1 {
public static void main(String[] args) throws Exception{
String source = "class C{ def printString: string; def k1: int; k1 = 1; def k2: int; k2 = 1; "
		+ "def n: int;  def temp: int; "
		+ "while (n > 0) { print printString; print k1; temp=k1+k2;"
		+ "k1 = k2; k2 =temp;"
		+ " n = n -1;}; print \"done\";}";
Codelet codelet = CodeletBuilder.newInstance(source);
CodeletBuilder.setString(codelet,"printString", "--");
CodeletBuilder.setInt(codelet, "n", 5);
int n = CodeletBuilder.getInt(codelet, "n");
System.out.println("Calling fib for "+n+" numbers");
codelet.execute();
n+=2;
CodeletBuilder.setInt(codelet, "n", n);
System.out.println("Calling fib for "+n+" numbers");
codelet.execute();

}
}