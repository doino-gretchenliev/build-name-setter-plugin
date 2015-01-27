package org.jenkinsci.plugins.buildnamesetter;

import hudson.Extension;
import hudson.Launcher;
import hudson.matrix.*;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.tasks.BuildWrapper;
import hudson.tasks.BuildWrapperDescriptor;
import org.jenkinsci.plugins.tokenmacro.MacroEvaluationException;
import org.jenkinsci.plugins.tokenmacro.TokenMacro;
import org.kohsuke.stapler.DataBoundConstructor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Set the name twice.
 *
 * Once early on in the build, and another time later on.
 *
 * @author Kohsuke Kawaguchi
 */
public class BuildNameSetter extends BuildWrapper implements MatrixAggregatable {

    public final String template;
    public final String template_matrix_head;

    @DataBoundConstructor
    public BuildNameSetter(String template, String template_matrix_head) {
        this.template = template;
        this.template_matrix_head=template_matrix_head;
    }

    public BuildNameSetter(String template) {
        this.template = template;
        this.template_matrix_head=null;
    }


    @Override
    public Environment setUp(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        setDisplayName(build, listener);

        return new Environment() {
            @Override
            public boolean tearDown(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
                setDisplayName(build, listener);
                return true;
            }
        };
    }

    private void setDisplayName(AbstractBuild build, BuildListener listener) throws IOException, InterruptedException {
        try {

            if (build instanceof MatrixBuild){
                build.setDisplayName(TokenMacro.expandAll(build, listener, template_matrix_head));
            }
            else {
                build.setDisplayName(TokenMacro.expandAll(build, listener, template));
            }

           // Boolean is_head=false;
           // try {
               // Method is_head_matrix = build.getClass().getMethod("getRuns");
                //is_head=true;
                //List runs= (List) is_head_matrix.invoke(build);


               /* if (runs!=null&&runs.size()>0){
                    build.setDisplayName(TokenMacro.expandAll(build, listener, template_matrix_head));
                }
                else {
                    build.setDisplayName(TokenMacro.expandAll(build, listener, template));

                }
                   }
            catch (NoSuchMethodException e){
                is_head=false;
            }
            catch (InvocationTargetException e) {
                e.printStackTrace();
            }*/



          /*  if(!is_head) {

                try {
                    build.setDisplayName(TokenMacro.expandAll(build, listener, template));
                  //  ((MatrixRun) build).getParentBuild().readResolve();
                }
                catch (Exception e ){
                    e.printStackTrace();
                }

            }*/


        } catch (MacroEvaluationException e) {
            listener.getLogger().println(e.getMessage());
        }
    }

    public MatrixAggregator createAggregator(MatrixBuild build, Launcher launcher, BuildListener listener) {
        return new MatrixAggregator(build,launcher,listener) {
            @Override
            public boolean startBuild() throws InterruptedException, IOException {
                setDisplayName(build,listener);
                return super.startBuild();
            }

            @Override
            public boolean endBuild() throws InterruptedException, IOException {
                setDisplayName(build,listener);
                return super.endBuild();
            }
        };
    }

    @Extension
    public static class DescriptorImpl extends BuildWrapperDescriptor {
        @Override
        public boolean isApplicable(AbstractProject<?, ?> item) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return "Set Build Name";
        }

        public boolean isMatrixJob(AbstractProject<?, ?> item) {
            if(item instanceof MatrixProject){
                return true;
            }
            else
                return false;
        }
    }
}
