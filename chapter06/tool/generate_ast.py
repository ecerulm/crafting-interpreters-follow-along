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


def define_ast(output_dir, types):
    template.stream(basename="Expr", types=types).dump(f"{output_dir}/Expr.java")


def main():
    types = [
        "Binary   : Expr left, Token operator, Expr right",
        "Grouping : Expr expression",
        "Literal  : Object value",
        "Unary    : Token operator, Expr right",
    ]
    define_ast("app/src/main/java/com/craftinginterpreters/lox", types)


if __name__ == "__main__":
    main()
