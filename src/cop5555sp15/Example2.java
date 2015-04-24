package cop5555sp15;
/** This class illustrates calling execute twice after modifying the int variable
* in the codelet. The expected output is
first time
0
2
second time
*/
public class Example2 {
public static void main(String[] args) throws Exception{
	String source = "class CallExecuteTwice{\n"
			+ "def i1: int;\n"
			+ "def first: boolean;\n"
			+ "while(i1 < 4) {"
			+ "if (first){print \"Du, du hast\"; print \"Du hast mich\";"
			+ "if((i1 ==1) | (i1 ==3)) {print \"\";};"
			+ "}\n"
			+ "else {print \"Du hast mich gefragt\";};\n"
			+ "i1 = i1 + 1;};}";
			Codelet codelet = CodeletBuilder.newInstance(source);
			CodeletBuilder.setBoolean(codelet, "first", true);
			codelet.execute();
			int i1 = CodeletBuilder.getInt(codelet, "i1");
			//System.out.println(i1);
			//System.out.println("\nonly second half\n");
			CodeletBuilder.setBoolean(codelet, "first", false);
			CodeletBuilder.setInt(codelet, "i1", 0);
		//	System.out.println(CodeletBuilder.getInt(codelet, "i1"));
			codelet.execute();
}
}