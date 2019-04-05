package life.qbic.cli;

import life.qbic.cli.main.MainCommand;
import life.qbic.cli.main.MainTool;
import org.junit.Test;

/**
 * Unit tests for MainTool.
 */
public class MainTest {
    // TODO: write unit tests (you do not need to test ToolExecutor, just test the execute() and shutdown() methods of your tool)

    @Test
    public void testSingleEnd() {
        String[] args = new String[3];
        args[0] = "-p=QGVIN";
        args[1] = "-o=new_folder/test";
        args[2] = "-c=/home/tlucas/IdeaProjects/geo-uploader-cli-new/config.yaml";

        final ToolExecutor executor = new ToolExecutor();
        //executor.invoke(MainTool.class, MainCommand.class, args);


    }

    @Test
    public void testPairedEnd() {

        String[] args = new String[3];
        args[0] = "-p=QJWAB";
        args[1] = "-o=new_folder/test";
        args[2] = "-c=/home/tlucas/IdeaProjects/geo-uploader-cli-new/config.yaml";

        final ToolExecutor executor = new ToolExecutor();
        //executor.invoke(MainTool.class, MainCommand.class, args);


    }

    @Test
    public void testMd5() {
        String[] args = new String[4];
        args[0] = "-p=QGVIN";
        args[1] = "-o=new_folder/test";
        args[2] = "-c=/home/tlucas/IdeaProjects/geo-uploader-cli-new/config.yaml";
        args[3] = "-m=true";

        final ToolExecutor executor = new ToolExecutor();
        //executor.invoke(MainTool.class, MainCommand.class, args);
    }
}