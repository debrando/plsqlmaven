package com.google.code.plsqlmaven;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import groovy.text.GStringTemplateEngine

/**
 * Creates source files by type
 *
 * @goal template
 *
 */
public class PlSqlTemplateMojo
    extends PlSqlMojo
{
    
   /**
    * Type of the object to be created (eg function,procedure,package)
    * @since 1.0
    * @parameter expression="${type}"
    * @required
    */
   private String type;

   /**
    * Name of the object to be created (eg myprc)
    * @since 1.0
    * @parameter expression="${name}"
    * @required
    */
   private String name;

    void execute()
    {
        try
        {
            connectToDatabase()
            
            sql.eachRow("select object_type from user_objects where object_name = upper(${name})")
            {
                fail("A ${it.object_type} already exists with this name")
            }
            
            disconnectFromDatabase()
        }
        catch (Error e)
        {
            // ignore if no error found
        }
        
        ant.mkdir(dir: project.build.sourceDirectory)
        
        def engine = new GStringTemplateEngine()
        name= name.toLowerCase();
        type= type.toLowerCase();
        
        if (! ((type =~ /^package/) || (type =~ /^type/)))
        {
           def type_dir= get_dir(project.build.sourceDirectory, type)
           def ext= get_type_ext(type)
           def tpl = this.getClass().getClassLoader().getResourceAsStream(type+".${ext}.sql")
           tpl= new BufferedReader(new InputStreamReader(tpl))
           def template = engine.createTemplate(tpl).make(['name': name])
           def target_file= new File(type_dir, name+".${ext}.sql")
           target_file << template.toString()
           log.info("created ${type}: "+target_file.absolutePath)
        }
        else
        {
           def type_dir= get_dir(project.build.sourceDirectory, type.split()[0])
           def odir= get_dir(type_dir, name)
           def ext= get_type_ext(type)
           def tpl = this.getClass().getClassLoader().getResourceAsStream(type+".${ext}.sql")
           tpl= new BufferedReader(new InputStreamReader(tpl))
           def template = engine.createTemplate(tpl).make(['name': name])
           def target_file= new File(odir, name+".${ext}.sql")
           target_file << template.toString()
           log.info("created ${type}: "+target_file.absolutePath)
           ext= get_type_ext(type+' body')
           tpl = this.getClass().getClassLoader().getResourceAsStream(type+".${ext}.sql")
           tpl= new BufferedReader(new InputStreamReader(tpl))
           template = engine.createTemplate(tpl).make(['name': name])
           target_file= new File(odir, name+".${ext}.sql")
           target_file << template.toString()
           log.info("created ${type} body: "+target_file.absolutePath)
        }

        
    }
}
