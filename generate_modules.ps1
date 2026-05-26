$categories = @("combat", "movement", "render", "player", "world", "exploit")
$adjectives = @("Auto", "Fast", "Super", "God", "Mega", "Ultra", "Hyper", "Quantum", "Anti", "Smart")
$nouns = @("Aura", "Clicker", "Breaker", "Placer", "Builder", "Destroyer", "Fly", "Speed", "Jump", "Step", "Glow", "ESP", "Tracer", "Bot", "Hack", "Bypass", "Spoof", "Phase", "Clip")

$count = 0
for ($i = 0; $i -lt 836; $i++) {
    $adj = $adjectives[(Get-Random -Maximum $adjectives.Length)]
    $noun = $nouns[(Get-Random -Maximum $nouns.Length)]
    $num = Get-Random -Minimum 1 -Maximum 9999
    $name = "$adj$noun$num"
    
    $cat_idx = Get-Random -Maximum $categories.Length
    $cat_lower = $categories[$cat_idx]
    $cat_upper = $cat_lower.ToUpper()
    
    $code = "package cc.quark.module.modules." + $cat_lower + ";`n"
    $code += "import cc.quark.module.Category;`n"
    $code += "import cc.quark.module.Module;`n`n"
    $code += "public class " + $name + " extends Module {`n"
    $code += "    public " + $name + "() {`n"
    $code += "        super(`"" + $name + "`", `"Advanced " + $name + " module`", Category." + $cat_upper + ");`n"
    $code += "    }`n"
    $code += "}`n"
    
    $path = "src\main\java\cc\quark\module\modules\$cat_lower\$name.java"
    
    Set-Content -Path $path -Value $code
    $count++
}

Write-Host "Generated $count modules!"
