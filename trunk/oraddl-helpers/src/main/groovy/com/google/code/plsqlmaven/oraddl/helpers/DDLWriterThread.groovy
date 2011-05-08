package com.google.code.plsqlmaven.oraddl.helpers

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

class DDLWriterThread extends Thread
{
       def ddl,log,helpers,mojo
       
       public DDLWriterThread(ddl,log,helpers,mojo)
       {
           this.ddl= ddl
           this.log= log
           this.helpers= helpers
           this.mojo= mojo
       }
       
       public void run()
       {
           def changes= mojo.changes
           log.info "final changes: "+changes.size()
           
           if (changes.size()>0)
           {
               log.info "generating DDL ${ddl.absolutePath}..."
               
               helpers.each{ changes= it.reorder(changes) }
               changes.each
               { 
                   change ->
                   
                   ddl << mlc(change.failSafe ? makeFailSafe(change.ddl) : change.ddl)+"\n/\n\n"
                    
               }
           }
       }
       
       private makeFailSafe(ddl)
       {
           def ddlEscaped= ddl.replaceAll("'","''");
           return mlc("""-- failsafe ddl
                         begin
                          execute immediate '${ddlEscaped}';
                         exception
                           when others then
                             null;
                         end;""")
       }
	   
      public mlc(multiLineText)
	  {   
		  if (multiLineText.indexOf("\n")==-1)
		    return multiLineText
			
		  def torm= (multiLineText =~ '(?m)^ *')
		  
		  if (torm.size()>1)
		    return multiLineText.replaceAll(torm[1],'')
      }

}
