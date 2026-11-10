# Et Futurum Requiem

# Note from Thenextelement:
This fork is primarily for use in my custom private pack and if you want different things than described feel free to fork this and do with it what you will.

# Current backported features this build has which are not present in the GTNH branch:  
/locate command, and /efrbuild <structurename> to build any world gen structure added by the mod
Shields, including banner decoration. 
End cities/Ships/Elytra/Dragon Head etc.  
All Experimental features on by default and the warnings removed. 
Nether biomes generate and function as intended, though the default biome is still called hell and not nether wastes. 
Lightning rod theoretically works.
All the Blocks that are "decoration only" in the GTNH branch have their expected Guis, fletching table now applies potions to arrows to make tipped arrows.
Stonecutter works, Smithing table does not yet have full parity with modern.
Cartography Table works, Loom works and all but 3 of the modern patterns have been added.
OUTSIDE OF THE LOWER ROCK LAYER/ORES AND A FEW DECORATIONS THERE IS NO OVERWORLD GENERATION YET.
Outer End islands generate with cities, odds are higher than vanilla as of now for both cities and ships.
Gateways generate once per dragon kill and function as intended.


# Planned:

# General: 
Mace/Spear/Trident and Applicable enchants, Modern swimming,




# Overworld: 
Deep dark/Ancient cities, will force disable Novacrafts if it is installed/they are enabled.
Trial Chambers.
Creaking/Pale Oak forests.
Woodland mansions that will disable Novacraft mansions via mixin if novacraft is installed and the mansion config is true, will still be disable-able via config. 
Pillager Outposts/Related mobs, these will be rare and raids will not be backported, or at least will be disabled by default because I personally am not a big fan of the mechanic.
Ruined Portals.


# Nether:
Bastions, Modern mobs like netherlicious adds, but the piglins won't attack you when wearing gold armor like they do in netherlicious for whatever reason, no clue if netherlicious added trading for them but that is planned here.
Also as a side note, EFR will currently disable its nether features when netherlicious is installed, I do want to supercede netherlicious eventually though, once i have all of the backported content done here i would like to invert the efr code and disable all but netherlicious' extra custom content because it is a few years out of date at this point.
Zombie piglins will probably not be added because Zombie Pigmen will be preserved.
Happy ghasts. 



# Maybe planned: 
Sniffers? Idk their implementation would be different because they're as good as useless in modern.
Some sort of sea creatures, a ray like i thought the phantom would be when i first saw the silhouette, modern fish.
Modern Command behaviour.





# NOT planned:
Phantoms.  if you want phantoms in 1.7 they can be enabled in Novacraft, though i don't think their behaviour matches modern.  You're lying if you like their vanilla behaviour tho xD

Sulfur Caves/Poplar forests.  Just not really a fan of the former, though the cubes would be fun, and the latter would be redundant with basically any biome mod.  New Overworld biomes will likely be ignored outside of the pale oak forests and 1.13+ Ocean generation.

World height extension.  Assuming the impossible becomes possible and Cubic chunks gains the ability to have broad sweeping support of legacy mods, that can be handled by them.  As much as I want to implement higher worlds it would be a waste of effort here and would reduce the very reasonably broad compat EFR currently has to effectively zero.

Fluid Logging/Chests being placed Next to each other.  
These features already exist in the following mods and I have never had any issues with Adjachest so I will continue using it, fluidlogging i have had issues with and i am not the biggest fan of the feature so i dont use it anymore but it does mostly work if memory serves:
https://github.com/GTMEGA/FluidLogged
https://github.com/Myask-sl/adjachest-1.7.10

Offhand: This fork already has explicit compat with GTNH Backhand for shield functionality, I see no reason to recreate that functionality in the mod.

Modern Combat: Absolutely not.  
Spam clicking all the way, 1.7 should remain as unbalanced as it's Halcyon days in my opinion.
Backports do exist for modern combat, that will never be included in this fork.




# Original Readme:


**Download: [CurseForge](https://www.curseforge.com/minecraft/mc-mods/et-futurum-requiem/files)
| [Modrinth](https://modrinth.com/mod/etfuturum/versions)**

WARNING!

Though this project is licensed under the LGPL-3.0 a lot of the code featured here is a direct copy or adaptation of
Mojangs original code. So be careful with what you do with it.

This project is jss2a98aj's fork of KryptonCaptain's Et Futurum build, as well as a merge of my own (Roadhog460's)
changes I made to Et Futurum and never published anywhere. Thanks to other incredible notable contributors such as
makamys among various things, including helping setup mixins, contributed hugely useful changes like porting the
expanded container code for Iron Shulker Boxes, and CI + nomixin to the gradle and GitHub scripts. And embeddedt who
contributed some incredibly large-scale backports like the Backlytra port and spectator mode, the F3 gamemode switcher
and other really good contributions.

The mod uses MCLib's AssetDirector module to download assets from Mojang's servers.
Check [its wiki page](https://github.com/makamys/MCLib/wiki/AssetDirector) for more information.

## Dependencies

- [UniMixins](https://modrinth.com/mod/unimixins)
  - Specifically, the GTNH module.
- [HogUtils](https://github.com/Roadhog360/HogUtils)
  - Currently EFR is in a transitional period as I get ready to use my new API. If you intend on using nightlies of EFR you will also need to download HogUtils in the same way.

## Contributing

To enable incomplete test features, add `-Detfuturum.testing=true` to your JVM arguments. This also enables a debug item

## Contributors
<a href="https://github.com/Roadhog360/Et-Futurum-Requiem/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=Roadhog360/Et-Futurum-Requiem" />
</a>

*The below is legacy information and is only kept for documentation purposes.*

### About `nomixin` builds (Obsolete)

From versions 2.4.1 to 2.6.0, the mod came in two flavors:

* The regular version embeds Mixin 0.7.11, allowing the mod to run standalone. However, this makes the jar a bit larger,
  and can cause problems in certain use cases.
* The version marked with `nomixin` doesn't embed Mixin, which lets it avoid these problems. But it requires a
  separate [Mixin bootstrap mod](https://gist.github.com/makamys/7cb74cd71d93a4332d2891db2624e17c#mixin-bootstrap-mods)
  to be installed in order to run. If you have one installed already, getting this version is recommended.

From those versions, mixin code will also not work if you do not
add `--tweakClass org.spongepowered.asm.launch.MixinTweaker --mixin mixins.etfuturum.json` to your program arguments.
