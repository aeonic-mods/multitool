# Engineer's Multitool

Engineer's Multitool adds a highly configurable, easily extensible and largely data-driven tool to your Minecraft world. On its own, it doesn't do much besides a lovely modern user interface for mode selection and the built-in fancy structure building mode (though you'll have to supply structures yourself as a mod- or pack-dev, detailed guide soon). 

However, the mod has a robust API for registering custom behaviors, as well as a data-driven recipe system for extending the internal ones. KubeJS support is planned in the *very* near future to register custom behaviors in a jiffy!

Some of the internal code will be moved to the API for external use - namely UI and configuration things - but as it stands they are left in the main source to denote these implementations are still volatile and you should not depend on them. The API contract itself is not entirely concrete yet either; suggestions for better behavior definitions and interactions is welcome and encouraged.

## Contributing

Make pull requests out to the dev branch. Issues describing the requested changes are encouraged before implementing or making PRs as well. I'm absolutely open to community contribution for anything from translations to config options to rewriting the whole implementation; we're big on the OSS hype train!

Some of the source's quirks might be a little unfamiliar in their current state; we use Registrate for registration as an artifact from the codebase I repurposed for this mod; I will translate everything to manual registration in the near future to avoid the overhead and extra dependency. Or you could! :)

With 💜 from Aeonic