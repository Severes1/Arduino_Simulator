import processing.core.*;
import java.util.ArrayList;
import java.io.File;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.util.Arrays;

public class File_Converter extends PApplet{
    private static String filename1;// = "Test_Arduino_simulator.ino";//"start_file.ino";
	private static String filename2 = "/java/Arduino_Program"; // .java;
    public static void main(String passedArgs[]){
        if (passedArgs.length >= 1){
            filename1 = passedArgs[0];
        }
        if (passedArgs.length >= 2){
            filename1 = passedArgs[1];
        }
        String[] appletArgs = new String[] { "File_Converter" };
		if (passedArgs != null) {
			PApplet.main(concat(appletArgs, passedArgs));
		} else {
			PApplet.main(appletArgs);
		}
    }

    int delays_count = 0;
    int last_delay_line = -1;
	
	String[][] replacements = {
			{ "Serial.print", "print"},
            { "Serial.begin", "//Serial.begin"} ,
			{ "INPUT", "parent.INPUT" },
			{ "OUTPUT", "parent.OUTPUT" },
			{ "HIGH", "parent.HIGH" },
			{ "LOW", "parent.LOW" }, { "const", "final" }, {"unsigned", ""}, {"long", "int"} 
            , {"delayMicroseconds", "delay"}};

	String[] keywords = { "abstract", "continue", "for", "new", "switch",
			"assert", "default", "goto", "package", "synchronized", "boolean",
			"do", "if", "private", "this", "break", "double", "implements",
			"protected", "throw", "byte", "else", "import", "public", "throws",
			"case", "enum", "instanceof", "return", "transient", "catch",
			"extends", "int", "short", "try", "char", "final", "interface",
			"static", "void", "class", "finally", "long", "strictfp",
			"volatile", "const", "float", "native", "super", "while" };

	public void setup() {
		String[] lines_array = loadStrings(filename1);
		ArrayList<String> local_variables = new ArrayList<String>();
		for (int i = 0; i < lines_array.length; i++) {
			if (lines_array[i].trim().length() > 2
					&& !lines_array[i].trim().substring(0, 2).equals("//")) {
				// println(lines_array[i].trim().substring(0,
				// 2));
				try {
					local_variables
							.add(split(
									match(lines_array[i],
											"(^int|boolean|String|double|double|long|void) \\w+")[0],
									' ')[1]);
				} catch (NullPointerException e) {
				}
			}
		}
		//println(local_variables);
		ArrayList<String> lines_list = new ArrayList<String>(
				Arrays.asList(lines_array));
		lines_list.add(0, "import processing.core.*;");
		lines_list.add(1, "public class Arduino_Program {");
		lines_list.add(2, "    Arduino_Simulator parent;");
		lines_list
				.add(3, "    Arduino_Program(Arduino_Simulator p){");
		lines_list.add(4, "        parent = p;");
		lines_list.add(5, "    }");
        lines_list.add(6, "int MD_SIM_VARIABLE_delayCount=0;");
        lines_list.add(7, "boolean MD_SIM_VARIABLE_stillRunning = true;");
		lines_list.add("}");

		for (int i = 8; i < lines_list.size() - 1; i++) {
        if (lines_list.get(i).trim().length() > 2
					&& !lines_list.get(i).trim().substring(0, 2).equals("//")){
			for (int j = 0; j < replacements.length; j++) {
				lines_list.set(
						i,
						lines_list.get(i).replaceAll(replacements[j][0],
								replacements[j][1]));
			}
			try {
				String keyword = match(lines_list.get(i), "\\w+\\s*\\(")[0]
						.substring(0, match(lines_list.get(i),
								"\\w+\\s*\\(")[0].length() - 1);
				if (!local_variables.contains(keyword)
						&& !Arrays.asList(keywords).contains(keyword.trim())) {
                  //  if (!(keyword.equals("delay") || keyword.equals("delay"))){
					//println("parent."+keyword);
					lines_list.set(
							i, lines_list.get(i).replaceAll(keyword+"\\s*\\(",
									"parent.$0"));
                    boolean contains_kw = false;
                    for (String kw : keywords){
                        if (lines_list.get(i).contains(kw)){
                            contains_kw = true; break;}
                    }
                    if (!contains_kw && !lines_list.get(i).contains("=") || lines_list.get(i).contains("print")){
                        lines_list.set(i, "if(MD_SIM_VARIABLE_stillRunning)\n"+lines_list.get(i));
                    }
              /*      } else {
                    // incorporate delays to work in a non asynchronous program
                    // This will not work if the delay is inside an if statement.
                    // TODO This always delays for 1000. Make it variable!
                        int k = i - 1;
                        String[] match = match(lines_list.get(i), "delay(.*)");
                        String val = match[1].trim().substring(1,match[1].trim().length()-2).trim();
                      //  lines_list.remove(i);
                        ArrayList<String> delayedCode = new ArrayList<String>();
                        while(lines_list.get(k).trim().length() < 1 || !(lines_list.get(k).trim().substring(lines_list.get(k).trim().length()-1).equals("}") || lines_list.get(k).trim().substring(lines_list.get(k).trim().length()-1).equals("{"))){
                            //delayedCode.add(0,lines_list.get(k));
                           // println(lines_list.get(k));
                            k -= 1;
                           // println(k);
                        }
                   //     lines_list.add(k+1, "    if (!parent.delaying() && MD_SIM_VARIABLE_delayCount == "+delays_count+") {");
                 //       lines_list.add(i + 1, "        setDelay("+val+");"); // ASSUMED THAT THE PROGRAM WILL CHANGE THIS TO parent.setDelay();
                  //      lines_list.add(i + 2, "        MD_SIM_VARIABLE_delayCount++;");
                 //       lines_list.add(i + 3, "    }");
                        //delayedCode.remove(0);
                        */
                        /*println("start:");
                        for (String s : delayedCode) println(s);
                        println(lines_list.get(i));*/
                        /*delays_count++;
                        last_delay_line = i + 2;
                    }*/
				}
				if (match(lines_list.get(i), "Serial").length > 0) {
					lines_list.set(i, "//" + lines_list.get(i));
				}
                
			}
			catch (NullPointerException e) {
			}
            try{
                if (!(lines_list.get(i).contains("="))){
                    String[] temp = match(lines_list.get(i), "\\b(int|double|boolean|float)\\b\\s(\\w+)\\[(.*)\\]");
                    if (temp.length > 0){
                        String t = split(temp[0], ' ')[0];
                        //println(temp[1]);
                        String n = split(temp[2], ' ')[0];
                        String z = temp[3];
                        //println(n);
                        if (!z.trim().equals("")){
                            lines_list.set(i, t + "[] " + n + " = new " + t + "[" + z + "];");
                        }
                       // println(t + "[] " + n + " = new " + t + "[" + z + "];");
                    }
                }
            } catch (NullPointerException e){
            }
            
			
		}
        if (i > 1) { // indentation
            lines_list.set(i, "    " + lines_list.get(i));
        }
        
        }/*
        if (delays_count > 0){
            int k = last_delay_line;
            ArrayList<String> delayedCode = new ArrayList<String>();
            while(lines_list.get(k).trim().length() < 1 || !lines_list.get(k).trim().substring(lines_list.get(k).trim().length()-1).equals("}")){
                delayedCode.add(lines_list.get(k));
               // println(lines_list.get(k));
                k += 1;
               // println(k);
            }
            println(lines_list.get(k));
            lines_list.add(last_delay_line + 2, "    if (!parent.delaying() && MD_SIM_VARIABLE_delayCount == "+delays_count+") {");
            //lines_list.add(k-1, "        setDelay("+val+");"); // ASSUMED THAT THE PROGRAM WILL CHANGE THIS TO parent.setDelay();
            lines_list.add(last_delay_line + 3, "        MD_SIM_VARIABLE_delayCount=0;");
            lines_list.add(k+1, "    }");
            //delayedCode.remove(0);
            
            println("start:");
            for (String s : delayedCode) println(s);
            delays_count++;
            //lines_list.remove(last_delay_line);
           // lines_list.add(last_delay_line, "MD_SIM_VARIABLE_delayCount = 0;");
        }*/
        lines_array = lines_list.toArray(new String[lines_list.size()]);
		// println(lines_array);
		saveStrings(filename2 + ".java", lines_array);
		// String[] args = {"arduino_simulator_v2.exe"};
        exit();
	}
}
