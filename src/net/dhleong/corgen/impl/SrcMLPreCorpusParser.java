package net.dhleong.corgen.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import net.dhleong.corgen.CorGen;
import net.dhleong.corgen.CorGen.Config;
import net.dhleong.corgen.CorGen.Language;
import net.dhleong.corgen.PreCorpus;
import net.dhleong.corgen.PreCorpusParser;
import net.dhleong.corgen.gran.ClassLevel;
import net.dhleong.corgen.gran.MethodLevel;
import net.dhleong.corgen.gran.PackageLevel;
import net.dhleong.corgen.gran.Word;
import net.dhleong.corgen.impl.SrcMLPreCorpusBuilder.SimpleFilter;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SrcMLPreCorpusParser reads in SrcML-created files and
 *  constructs a PreCorpus from them. It is thread-safe
 * 
 * @author dhleong
 *
 */
public class SrcMLPreCorpusParser 
implements PreCorpusParser {
    
    private final SimpleFilter mFilter;
    private final Comparator<File> mComp = new Comparator<File>() {

        @Override
        public int compare(File arg0, File arg1) {
            // mini hack to force .h files to get parsed before .cpp files
            String ext0 = arg0.getName().substring(arg0.getName().lastIndexOf('.')+1);
            String ext1 = arg1.getName().substring(arg1.getName().lastIndexOf('.')+1);
            return ext1.compareTo(ext0);
        }
        
    };
    
    public SrcMLPreCorpusParser(Language language) {
        mFilter = new SimpleFilter(language);
    }

    @Override
    public PreCorpus parse(File srcRoot, CorGen.Config config) {
        BasicPreCorpus corp = new BasicPreCorpus();
        ParseHandler handler = new ParseHandler(corp, config);
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = factory.newSAXParser();
            
            Deque<File> workList = new ArrayDeque<File>();
            workList.addLast(srcRoot);
            
            while (workList.size() > 0) {
                File curr = workList.pop();
                if (curr.isDirectory()) {
                    // scan for sub-directories to place on the work list
                    File[] found = curr.listFiles(mFilter);
                    Arrays.sort(found, mComp );
                    for (File f : found) {
                        workList.addLast(f);
                    }
                } else {
                    // parse the file
                    String relName = curr.getAbsolutePath().substring(
                            srcRoot.getAbsolutePath().length());
                    handler.setPath(relName);
                    
                    PushbackInputStream inputStream = new PushbackInputStream(
                            new FileInputStream(curr), 1);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)); 
//                    UnicodeReader reader = new UnicodeReader(inputStream, "UTF-8");                    
                     
                    try {
//                        // eat any junk before the prolog
//                        while (reader.read() != '<') {}
//                        inputStream.unread('<');
                        
                        // create the input source and go
                        InputSource is = new InputSource(reader);
                        is.setEncoding("UTF-8");
                        saxParser.parse(is, handler);
                    } catch (IOException e) {
                        // we probably want to continue with other files anyway...?
                        System.err.println("WARN: IO Exception in " + relName);
                        e.printStackTrace();
                    } catch (SAXParseException e) {
                        // we probably want to continue with other files anyway...?
                        System.err.println("WARN: Parse Exception in " + relName);
                        e.printStackTrace();
                    } 
                }
            }
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SAXException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            // should never happen
            e.printStackTrace();
        }
        
        return corp;
    }

    /**
     * XML Streaming Handler for doing the actual parsing
     *  of SrcML files with minimal overhead. 
     *  
     * @author dhleong
     *
     */
    public class ParseHandler extends DefaultHandler {
        
        private String mPackageName;
        
        StringBuilder buffer;
        
        private final BasicPreCorpus corp;
        private final Config config;
        
        private PackageLevel currentPackage;
        
        private final Deque<NestedClass> mNested = new ArrayDeque<NestedClass>();
        private NestedClass mCurrentNest;

        public ParseHandler(BasicPreCorpus corp, Config config) {
            this.corp = corp;
            this.config = config;
            
            clear();
        }

        public void setPath(String relName) {
            // FIXME Some config option to pick between "folder-projects" and "filename-projects"
//            mPackageName = "project:" + relName.substring(0, relName.lastIndexOf('/'));
            mPackageName = "project:" + relName;
            currentPackage = new PackageLevel(mPackageName);
            currentPackage.add(new ClassLevel(mPackageName, "FUNCTION"));
            corp.add(currentPackage);
            if (config.verbose) 
                System.out.println("\\ package: " + mPackageName);
            
            mCurrentNest = new NestedClass(currentPackage, mPackageName, config);
            // clear the stack since we're in a new package
            mNested.clear();
        }
        
        @Override
        public void startElement(String uri, String localName, String qName, 
                Attributes attributes) throws SAXException {
            
            if (mCurrentNest.inClass && "class".equals(qName)) {
                // move old nest to the stack
                mNested.push(mCurrentNest);
                
                // new nest
                mCurrentNest = new NestedClass(currentPackage, mPackageName,
                        config, mCurrentNest);
            }
            mCurrentNest.startElement(qName, this, corp);
        }

        @Override
        public void endElement(String uri, String localName,
                String qName) throws SAXException {
            
            // check if we're nested (IE stack > 0)
            if ("class".equals(qName) && mNested.size() > 0) {
                
                // pop the stack
                mCurrentNest = mNested.pop();
                
                // if the nest is empty, it's fine if we
                //  just continue to use the same one
            }
            
            mCurrentNest.endElement(qName, this, corp);
        }

        @Override
        public void characters(char ch[], int start, int length) throws SAXException {
            buffer.append(ch, start, length);
        }
    
        /** clear the buffer */
        private void clear() {
            // apparently this is the best way...
            buffer = new StringBuilder(64);
        }
        
        /** get a string from the buffer */
        private String make() {
            return buffer.toString().trim()
                .replace('[', ' ') // some definite filters?
                .replace(']', ' ');
        }    
    }
    
    /**
     * To handle nested classes, we store pretty much all
     *  "current" parsing state in one of these, and maintain
     *  a stack that gets pushed to and popped as appropriate
     *   
     * @author dhleong
     *
     */
    private static class NestedClass {
        
        private static final Pattern EXPR_CLEANER = Pattern.compile("[^a-zA-Z0-9_:]+");
        
        private final String mPackageName;
        private final boolean useComments, verbose;
        private String mParentName = null, mName = null;
        
        private final PackageLevel mPackage;
        private ClassLevel mClass;
        private MethodLevel mMethod;
        
        private String mCommentBuffer;
        private boolean mCommentReserved = false;
        
        boolean inClass = false, classNamed=false;
        boolean inMethod = false, methodNamed=false;
        int inName = 0; // names can get nested
        int inExpr = 0; // expressions also get nested (sad face) 
        boolean inType = false;
//        private boolean inComment = false;

        public NestedClass(PackageLevel currentPackage, String packageName, Config config) {
            this(currentPackage, packageName, config, null);
        }
        
        public NestedClass(PackageLevel currentPackage, String packageName,
                Config config, NestedClass parent) {
            mPackage = currentPackage;
            mPackageName = packageName;
            this.useComments = config.useComments;
            this.verbose = config.verbose;
            
            if (parent != null) {
                mParentName = parent.getName();
                
                // copy the comment buffer, since this
                //  nested class could have block comments
                if (!parent.mCommentReserved)
                    mCommentBuffer = parent.mCommentBuffer;
            }
        }
        
        public String getName() {
            return mName;
        }

        public void startElement(String qName, ParseHandler parser,
                BasicPreCorpus corp) {
            
            if ("class".equals(qName)) {
                
                inClass = true;
                
                if (mCommentBuffer != null && !mCommentReserved) {
                    mCommentReserved = true;
                    // since we haven't made the class yet, reserve it
                }
            } else if ("name".equals(qName)) {
//                if ()
                if (inExpr > 0) 
                    processExpr(parser, corp); // handle whatever was in the buffer
                    
                if (inName == 0)// && inClass && !classNamed)
                    parser.clear(); // definitely new name... trash the buffer
                
                inName++;
                
            } else if ("expr".equals(qName)) {
                
                if (inExpr == 0 && inName == 0)
                    parser.clear(); // definitely new expr... trash the buffer
                
                inExpr++;
              
            } else if (processedAsMethod(qName)) {
                inMethod = true;
                
                if (mCommentBuffer != null && !mCommentReserved) {
                    mCommentReserved = true;
                    // since we haven't made the function yet, reserve it
                }
            } else if ("type".equals(qName)) {
                inType = true;
            } else if (useComments && "comment".equals(qName)) {
                // prepare for adding comments 
//                inComment = true;
                parser.clear();
            } else if ("argument_list".equals(qName) && inName > 0) {
                // eg: <name>vector<argument_list>&lt;<name>...</argument_list></name>
                inName--;
                
                processName(parser, corp);
            }
            
            if (mCommentBuffer != null && !mCommentReserved) {
                // if we didn't use up the comment buffer
                //  right away, it wasn't "attached" to
                //  a method, and should be added to the
                //  current class/package
                if (inClass && mClass != null) {
                    // cool, add to the class
                    for (String w : cleanSplit(mCommentBuffer))
                        mClass.add(new Word(w));
                } else if (parser.currentPackage != null) {
                    // okay, it's just floating in the package
                    //  somewhere (as far as we're concerned)
                    // package *shouldn't* be null ever, but
                    //  just  in case....
                    for (String w : cleanSplit(mCommentBuffer))
                        parser.currentPackage.add(new Word(w));
                }
                    
                
                // clear it
                mCommentBuffer = null;
            }
        }
        
        /**
         * Clean a string (that could be from comments, etc.)
         *  and split it into words
         * @param mCommentBuffer2
         * @return
         */
        private String[] cleanSplit(String buffer) {
            String filtered = EXPR_CLEANER.matcher(buffer).replaceAll(" ");
            return filtered.split("\\s+");
        }

        public void endElement(String qName, ParseHandler parser, 
                BasicPreCorpus corp) {
            if ("class".equals(qName)) {
                inClass = false;
                classNamed = false;
            } else if ("name".equals(qName)) {
                inName--;
                
//                if (inExpr > 0)
//                    return; // let the expr handle it (?)
                
                if (inName > 0)
                    return; // not done... keep going
                else if (inName < 0) {
                    // hit an argument list
                    inName = 0;
                    return;
                }
                
                processName(parser, corp);
            } else if ("expr".equals(qName)) {
                inExpr--;
                
                if (inExpr > 0) 
                    return; // not done... keep going (..?  does it matter?)
                else if (inExpr < 0) {
                    // would this happen?
                    inExpr = 0;
                    return;
                }
                processExpr(parser, corp);
              
            } else if (processedAsMethod(qName)) {
                inMethod = false;
                methodNamed = false;
            } else if ("type".equals(qName)) {
                inType = false;
            } else if (useComments && "comment".equals(qName)) {
//                inComment = false;
                String filtered = parser.make()
                    .replace('/', ' ')
                    .replace('*', ' ');
                if (inMethod && mMethod != null) {
                    // add words to the method, easy
                    for (String w : cleanSplit(filtered))
                        mMethod.add(new Word(w));
                } else {
                    // store in a comment buffer...
                    //  if the next element we see is a method,
                    //  we can add it to the method. Otherwise,
                    //  add it to the class if available, falling
                    //  back finally to the package
                    mCommentBuffer = filtered;
                    
                    // it could also be comments between the declaration
                    //  and the name of the function... we may just
                    //  ignore these, since they're probably irrelevant anyway
                }
            }
             
//            clear();
        }
        
        /**
         * Check if the given qName should be parsed as a method
         * @param qName
         * @return True if (duh)
         */
        private static final boolean processedAsMethod(String qName) {
            return "function".equals(qName) || "function_decl".equals(qName) || 
                "constructor".equals(qName) || "constructor_decl".equals(qName) || 
                "destructor".equals(qName) || "destructor_decl".equals(qName);
        }
        
        private void processExpr(ParseHandler parser, BasicPreCorpus corp) {
            String[] words = cleanSplit(parser.make());
            
            if (inMethod && mMethod != null) {
                for (String w : words)
                    mMethod.add(new Word(w));
            } else if (inClass && mClass != null) {
                // give it to the class
                for (String w : words)
                    mClass.add(new Word(w));
            } else {
                // package level
                for (String w : words)
                    mPackage.add(new Word(w));
            }
        }

        private void processName(ParseHandler parser, BasicPreCorpus corp) {
//          System.out.println("end name: " + make());
          String name = parser.make();
          
          if (inClass) {
              if (!classNamed) {
                  if (mParentName != null)
                      name = mParentName + "$" + name;
                  if (verbose) System.out.println(" + class: " + name);
                  classNamed = true;
                  mClass = new ClassLevel(mPackageName, name);
                  corp.add(mClass);
                  mName = name;
                  
                  if (mCommentReserved) {
                      // we've reserved comments (I think?)
                      for (String w : cleanSplit(mCommentBuffer))
                          mClass.add(new Word(w));
                      mCommentReserved = false;
                      mCommentBuffer = null;
                  }
              } else if (!inMethod) {
                  // add word to class
                  mClass.add(new Word(name));
              }
          }
          
          if (inMethod) {
              // we've started a method but have yet to
              //  get a name for it, and we're in a type...
              //  ignore it
              if (!methodNamed && inType)
                  return;
              else if (!methodNamed) {
                  // woo, name the method
                  if (verbose) System.out.println(" + method: " + name);
                  methodNamed = true;
                  
                  // figure out what class to assocate with
                  String klass = "FUNCTION"; // magic fake class for non-methods
                  int sepPos;
                  if (inClass) 
                      klass = mClass.getName();
                  else if ((sepPos = name.indexOf(':')) != -1)
                      klass = name.substring(0, sepPos);
                  
                  mMethod = new MethodLevel(mPackageName, klass, name);
                  corp.add(mMethod);
                  
                  if (mCommentReserved) {
                      // we've reserved comments (I think?)
                      for (String w : cleanSplit(mCommentBuffer))
                          mMethod.add(new Word(w));
                      mCommentReserved = false;
                      mCommentBuffer = null;
                  }
              } else {
                  // add word to the method
                  mMethod.add(new Word(name));
              }
          }
      }
    }

}
