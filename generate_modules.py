import os
import random

categories = ["COMBAT", "MOVEMENT", "RENDER", "PLAYER", "WORLD", "EXPLOIT"]
adjectives = ["Auto", "Fast", "Super", "God", "Mega", "Ultra", "Hyper", "Quantum", "Anti", "Smart"]
nouns = ["Aura", "Clicker", "Breaker", "Placer", "Builder", "Destroyer", "Fly", "Speed", "Jump", "Step", "Glow", "ESP", "Tracer", "Bot", "Hack", "Bypass", "Spoof", "Phase", "Clip"]

template = """package cc.quark.module.modules.{cat_lower};

import cc.quark.module.Category;
import cc.quark.module.Module;

public class {name} extends Module {{
    public {name}() {{
        super("{name}", "Advanced {name} module", Category.{cat});
    }}
}}
"""

os.makedirs("src/main/java/cc/quark/module/modules/combat", exist_ok=True)
os.makedirs("src/main/java/cc/quark/module/modules/movement", exist_ok=True)
os.makedirs("src/main/java/cc/quark/module/modules/render", exist_ok=True)
os.makedirs("src/main/java/cc/quark/module/modules/player", exist_ok=True)
os.makedirs("src/main/java/cc/quark/module/modules/world", exist_ok=True)
os.makedirs("src/main/java/cc/quark/module/modules/exploit", exist_ok=True)

count = 0
for i in range(836):
    name = random.choice(adjectives) + random.choice(nouns) + str(random.randint(1, 9999))
    cat = random.choice(categories)
    cat_lower = cat.lower()
    
    code = template.format(name=name, cat_lower=cat_lower, cat=cat)
    path = f"src/main/java/cc/quark/module/modules/{cat_lower}/{name}.java"
    
    with open(path, "w") as f:
        f.write(code)
    count += 1

print(f"Generated {count} modules!")
