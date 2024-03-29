package net.dhleong.corgen.plugins;

public class CppStopWordRemovalPlugin extends StopWordRemovalPlugin {

    @Override
    public String[] getStopWords() {
        return new String[] {
                // c
                "auto", "const", "double", "float", "int",
                "short", "struct", "unsigned", "break", "continue",
                "else", "for", "long", "signed", "switch", "void",
                "case", "default", "enum", "goto", "register", 
                "sizeof", "typedef", "volatile", "char", "do",
                "extern", "if", "return", "static", "union", "while",
                
                // new for cpp
                "asm", "dynamic_cast", "namespace", "reinterpret_cast",
                "try", "bool", "explicit", "new", "static_cast", "typeid",
                "catch", "false", "operator", "template", "typename",
                "class", "friend", "private", "this", "using", "const_cast",
                "inline", "public", "throw", "virtual", "delete", "mutable",
                "protected", "true", "wchar_t"
        };
    }

}
