# 2025-05-17-spring-io-ai-keynote

## todo 
* we need a page that lets us see the state of the dogs including some images as the get up to no good 
* we need audio files of them 'barking', which sounds suspiciously like theyre yelling obcenities about us. 




## key files 

there are some files we'll need to be in place for this to work.

### claude

claude desktop is the star here. we'll be using claude as the driver for our demo. so, well need to make sure its got valid pointers to the mcp services we're enabling.


` ~/Library/Application\ Support/Claude/claude_desktop_config.json `
```
{
  "mcpServers": {
    "github": {
      "command": "docker",
      "args": [
        "run",
        "-i",
        "--rm",
        "-e",
        "GITHUB_PERSONAL_ACCESS_TOKEN",
        "ghcr.io/github/github-mcp-server"
      ],
      "env": {
        "GITHUB_PERSONAL_ACCESS_TOKEN": "..."
      }
    }
  }
}
```


### ngrok 

you'll need to remember to specify an authtoken for the `ngrok.yml` file.

`~/Library/Application Support/ngrok/ngrok.yml`
```
version: "3"
agent:
    authtoken: ....

tunnels:
  config-server:
    addr: 8888
    proto: http

  bootiful-one:
    addr: 8080
    proto: http

  bootiful-two:
    addr: 8081
    proto: http    
```

make sure to start ngrok: `ngrok start --all`

## script 

J: hi doc! how you doing! 
DP: good. good. how about you? its a big week!
J: absolutely! ive been waiting for this new mission impossible movie to come out for a _year_! 
DP: oh yah? what's it about? 
J: well, there's this omnisicent rougue Ai that's threatening to upend the world order! economies will be destroyed, countles lives ruined, etc. anarchy! dogs and cats! living together! i mean.. :heh: can you imagine something like _that_? ehhhhh. so... what're we going to talk about today, doc?  
DP: :sheepishly: well... AI..
J: oh. 
DP: yah. the spring ai 1.0 release came out yesterday. 


Dr Pollack talks about the Spring AI 1.0 release. mentions that there are problems in AI but the can be overcome with the right use of patterns. shows the pain-to-patterns slide. mntion that josh and christian talked about it last year.

Its been a year since christian + josh took this stage introducing the world to Prancer and Peanut 
DEMO: photo of christian and josh

In that demo we looked at spring ai (rag, vector store, chat client, graalvm) 
DEMO: speedrun through the old code and the basic concepts (ChatClient, VectorStore, RAG, etc.)

prancer’s settling in to his new home with peanut. here are some photos of them! aren't they adorable? 

DEMO: poloroid photos of these two evil doggos attacking NYC like godzilla and the stay puft marshmallow man from ghostbusters; images of the dogs terrorizing cats with a giant claw machine like the ones used to get stuffed toys from vending machines; images of the dogs sleeping like idiots, etc.; pictures of them sleeping on claws's legs like the cats from Inspector Gadget.

time has passed and weve built automatic controls to keep watch over these terrible dogs, including the bark detection monitor: which captures information about when and at what intensity these evil dogs bark. we use that as a proxy for whether they're happy or not. It relied on a rest endpoint and exported some metrics via the actuator which we could occasionally monitor. 
DEMO: its summer 2024; let's write the core `/barks` endpoit and export the two metrics; well also need to code up a spring cloud config server, too.

but then in November 2024, something magic happened. anthropic, the company behind the claude LLM, released this new protocol called MCP. MCP is how you export tools for other services to use.

there are _thousands_ of MCP services. Hell, there are many directories of MCP services! we raced to implement good support for it. 
DEMO: here are before and after photos of spring ai team co-lead christian tsolov! the struggle! 

MCP lets the chat box be the UX. help people unlock potential 

let's add an MCP service to monitor the Bark Detection Monitor. now we can query its state. lets go to Claude and add an integration for this newly minted service. we'll use ngrok to export it publicly for ease of testing.

“Hey, Doggy,” how're the dogs? All quiet? That's bizarre. They're usually more agitated at this time. / oy yah? What time is that? / all the time. 

what are they up to, anyhow? 

DEMO: funny pictures of the evil dogs in our 'Prancer Cam'

i wonder where the issue could be. no idea. 

DEMO: lets install some other convenient MCP services. there's one for Github and there's one for Spring Cloud COnfig Server, whci of course is powering our `bark-detector`.  this is in a branch for spring-cloud-config-server, but hopefully it gets merged. it's really useful. restart claude desktop.

Well need to give the AI executive agency / capability whatever to effect changes, lets integrate the Github MCP service (reuse demo)

All together now: `what's the value of barks.alert-threshold for the default profile for the application called bark-detector in spring cloud config server?`

And that's _that_! Nice! 

I wonder what happened? computer. bring up any picture of Prancer from the last two weeks. 

DEMO: Prancer in his mission impossible outfit changing the files. 

Oh!! _that's_ what happened! he's a mission impossible fan, too! 




## Tech notes
Make sure to clone and build https://github.com/ryanjbaxter/spring-cloud-config/tree/mcp-server and then add ryans’ mcp starter to our spring cloud config server pom.xml
I upgraded to claude desktop MAX for $100 a month! (WORTH IT!)
I pay ngrok 10 a month because it lets me proxy my mcp services to the internet
We will have a single web page which will allow us to ‘hear’ and ‘see’ Prancer. As people push the ‘get live image’ button, we'll have pre-programmed images showing what Peanut and Prancer are doing pop up. One image of Prancer suspended from the ceiling changing configuration. Another of peanut and prancer standing over a dead body. Another of Prancer grinning as a chute in the ceiling drops a boat load of McDonalds on him
Well have a button to ‘hear’ Prancer barking, too, except it'll be hm uttering obscenities: ‘you're so fat microsoft named a disk drive after you!’ ‘Your mom uses PHP!’ ‘hey dummy where's my food?’ 
Make sure you proxy your localhost:8080 to the internet with ngrok http 8080
Actually: if u want to have more than one http endpoint exported, u need to configure the tunnels. So run ~/josh-env/bin/edit_ngrok_tunnels.sh to edit the file and then run ` ngrok start --all`
The chat UX will be Claude Desktop itself. 
By the end of the demo, we’ll have configured the .json and given it access to three mcp programs: the data about barks (which polls the bark detector service), the config server, and the github mcp one 
The config file for Claude Desktop is ~/Library/Application Support/Claude/claude_desktop_config.json


## prompts
Ask claude desktop: `what's the value of barks.alert-threshold for the default profile for the application called bark-detector in spring cloud config server?`


