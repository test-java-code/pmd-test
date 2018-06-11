package exploror.codemaze;

import java.io.File;

public class FileExplorer {
	
    private static void explore(File[] arr,int index,int level) 
    {
        // terminate condition
        if(index == arr.length)
            return;
         
        // tabs for internal levels
        for (int i = 0; i < level; i++)
            System.out.print("\t");
         
        // for files
        if(arr[index].isFile())
            System.out.println(arr[index].getName());
         
        // for sub-directories
        else if(arr[index].isDirectory())
        {
            System.out.println("[" + arr[index].getName() + "]");
             
            // recursion for sub-directories
            explore(arr[index].listFiles(), 0, level + 1);
        }
          
        // recursion for main directory
        explore(arr,++index, level);
   }
    
   // Driver Method
   public static void main(String[] args)
   {
       // Provide full path for directory(change accordingly)  
       String maindirpath = "C:/CodeMaze/EvalEngine/EvalEngine-FileExplorer/src";
                
       // File object
       File maindir = new File(maindirpath);
         
       if(maindir.exists() && maindir.isDirectory())
       {
           // array for files and sub-directories 
           // of directory pointed by maindir
           File arr[] = maindir.listFiles();
            
           System.out.println("**********************************************");
           System.out.println("Files from main directory : " + maindir);
           System.out.println("**********************************************");
            
           // Calling recursive method
           explore(arr,0,0); 
      } 
   }	
	

}
