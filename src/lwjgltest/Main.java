package lwjgltest;
import org.lwjgl.BufferUtils;
import org.lwjgl.Sys;
import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;

import Util3D.Point2dl;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.*;
import static org.lwjgl.system.MemoryUtil.*;

public class Main {

    public class MouseHandler{
        public DoubleBuffer xbuf;
        public DoubleBuffer ybuf;
        public Point2dl pos;
        public ArrayList<Point2dl> posStack;
        public final long posStackSize = 255;
        public final long window;

        MouseHandler(final long windowIn){
            window = windowIn;
            xbuf = BufferUtils.createDoubleBuffer(1);
            ybuf = BufferUtils.createDoubleBuffer(1);
            pos = new Point2dl(0,0);
        }

        public Point2dl update(){
            long x, y;
            glfwGetCursorPos(window, xbuf, ybuf);
            x = (long) xbuf.get();
            y = (long) ybuf.get();
            xbuf.clear();
            ybuf.clear();
            Point2dl newPos = new Point2dl(x, y);
            if(! newPos.equals(pos)) {
                pos = newPos;
                //System.out.println("Mouse:" + pos);
            }
            return pos;
        }
    }

    //
    // Utils
    //
    int CreateShader(int shadertype,String shaderString){
        int shader = GL20.glCreateShader(shadertype);
        GL20.glShaderSource(shader, shaderString);
        GL20.glCompileShader(shader);
        int status = GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS);
        if (status == GL11.GL_FALSE){
            String error = GL20.glGetShaderInfoLog(shader);
            String ShaderTypeString = null;
            switch(shadertype)
            {
                case GL20.GL_VERTEX_SHADER: ShaderTypeString = "vertex"; break;
                case GL32.GL_GEOMETRY_SHADER: ShaderTypeString = "geometry"; break;
                case GL20.GL_FRAGMENT_SHADER: ShaderTypeString = "fragment"; break;
            }

            System.err.println( "Compile failure in %s shader:\n%s\n"+ShaderTypeString+error);
        }
        return shader;
    }

    public int createShaderProgramme(int[] shadertypes, String[] shaders){
        int[] shaderids= new int[shaders.length];
        for (int i = 0; i < shaderids.length; i++) {
            shaderids[i]=CreateShader(shadertypes[i], shaders[i]);
        }
        return createShaderProgramme(shaderids);
    }

    public int createShaderProgramme(int[] shaders){
        int program = GL20.glCreateProgram();
        for (int i = 0; i < shaders.length; i++) {
            GL20.glAttachShader(program, shaders[i]);
        }
        GL20.glLinkProgram(program);

        int status = GL20.glGetShaderi(program, GL20.GL_LINK_STATUS);
        if (status == GL11.GL_FALSE){
            String error=GL20.glGetProgramInfoLog(program);
            System.err.println( "Linker failure: %s\n"+error);
        }
        for (int i = 0; i < shaders.length; i++) {
            GL20.glDetachShader(program, shaders[i]);
        }
        return program;
    }


    // We need to strongly reference callback instances.
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback   keyCallback;

    // The window handle
    private long window;
    MouseHandler mouse;

    // GPU assets
    private int buffer;
    private int shader;

    public void run() {
        System.out.println("Hello LWJGL " + Sys.getVersion() + "!");

        try {
            init();
            loop();

            // Release window and window callbacks
            glfwDestroyWindow(window);
            keyCallback.release();
        } finally {
            // Terminate GLFW and release the GLFWerrorfun
            glfwTerminate();
            errorCallback.release();
        }
    }



    private void initScene() {
        float[] data= new float[]{
                -0.5f, -0.5f, 0.f,
                -0.5f,  0.5f, 0.f,
                0.5f, -0.5f, 0.f,
                0.5f,  0.5f, 0.f,
                -0.5f,  0.5f, 0.f,
                0.5f, -0.5f, 0.f,
        };

        FloatBuffer dataBuffer = BufferUtils.createFloatBuffer(data.length);
        dataBuffer.put(data);
        dataBuffer.flip();

        buffer = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, buffer);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, dataBuffer, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        ////////////////Prepare the Shader////////////////
        String vert=
                "#version 330                                \n"+
                        "in vec3 position;                            \n"+
                        "void main(){                                \n"+
                        "    gl_Position= vec4(position,1);        \n"+
                        "}                                            \n";
        String frag=
                "#version 330                                \n"+
                        "out vec4 out_color;                        \n"+
                        "void main(){                                \n"+
                        "    out_color= vec4(0f, 1f, 1f, 1f);        \n"+
                        "}                                            \n";
        shader = createShaderProgramme(new int[]{
                GL20.GL_VERTEX_SHADER,GL20.GL_FRAGMENT_SHADER
        }, new String[]{
                vert,frag
        });

    }

    private void init() {
        // Setup an error callback. The default implementation
        // will print the error message in System.err.
        glfwSetErrorCallback(errorCallback = errorCallbackPrint(System.err));

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( glfwInit() != GL11.GL_TRUE )
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure our window
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable

        int WIDTH = 300;
        int HEIGHT = 300;

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, "Hello World!", NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, keyCallback = new GLFWKeyCallback() {
            @Override
            public void invoke(long window, int key, int scancode, int action, int mods) {
                if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                    glfwSetWindowShouldClose(window, GL_TRUE); // We will detect this in our rendering loop
            }
        });


        // Get the resolution of the primary monitor
        ByteBuffer vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        // Center our window
        glfwSetWindowPos(
                window,
                (GLFWvidmode.width(vidmode) - WIDTH) / 2,
                (GLFWvidmode.height(vidmode) - HEIGHT) / 2
        );

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);
        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        // This line is critical for LWJGL's interoperation with GLFW's
        // OpenGL context, or any context that is managed externally.
        // LWJGL detects the context that is current in the current thread,
        // creates the ContextCapabilities instance and makes the OpenGL
        // bindings available for use.
        GLContext.createFromCurrent();

        mouse = new MouseHandler(window);

        initScene();
    }

    private void renderScene(){
        GL20.glUseProgram(shader);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, buffer);
        GL20.glBindAttribLocation(shader, 0, "position");
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 0, 0);

        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);

        GL20.glDisableVertexAttribArray(0);
        GL20.glUseProgram(0);
    }

    private void handleUserInput(){
        mouse.update();
    }

    private void loop() {
        // Set the clear color

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( glfwWindowShouldClose(window) == GL_FALSE ) {

           handleUserInput();

            glClearColor(1.0f, 0.0f, 0.0f, 0.0f);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer

            renderScene();

            glfwSwapBuffers(window); // swap the color buffers

            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    public static void main(String[] args) {
        new Main().run();
    }

}