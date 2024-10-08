import os

from jinja2 import (Environment, FileSystemLoader, PackageLoader,
                    select_autoescape)

env = Environment(
    loader=FileSystemLoader(os.path.join(os.path.dirname(__file__), "templates")),  #
)

env.trim_blocks = True
env.lstrip_blocks = True


def split(value, sep="", maxsplit=-1):
    to_return = value.split(sep=sep, maxsplit=maxsplit)
    to_return = [v.strip() for v in to_return]

    return to_return


env.filters["split"] = split

template = env.get_template("Expr.java")


def define_ast(output_dir, basename, types):
    template.stream(basename=basename, types=types).dump(f"{output_dir}/{basename}.java")


def main():
    expr_types = [
        "Assign   : Token name, Expr value",
        "Binary   : Expr left, Token operator, Expr right",
        "Grouping : Expr expression",
        "Literal  : Object value",
        "Logical  : Expr left, Token operator, Expr right",
        "Unary    : Token operator, Expr right",
        "Variable : Token name",
    ]
    outputDir = "app/src/main/java/com/craftinginterpreters/lox"
    define_ast(outputDir, basename="Expr", types=expr_types)

    stmt_types = [
        "Block        : List<Stmt> statements",
        "Expression   : Expr expression",
        "If           : Expr condition, Stmt thenBranch, Stmt elseBranch",
        "Print        : Expr expression",
        "Var          : Token name, Expr initializer",
        "While        : Expr condition, Stmt body",
    ]

    define_ast(outputDir, "Stmt", stmt_types)



if __name__ == "__main__":
    main()
